package com.dcmc.apps.taskmanager.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.PREFERRED_USERNAME;

import com.dcmc.apps.taskmanager.repository.AuthorityRepository;
import com.dcmc.apps.taskmanager.repository.UserRepository;
import com.dcmc.apps.taskmanager.security.AuthoritiesConstants;
import com.dcmc.apps.taskmanager.security.JwtUserSyncFilter;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.security.oauth2.AudienceValidator;
import jakarta.servlet.Filter;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    public SecurityConfiguration(
        JHipsterProperties jHipsterProperties,
        UserRepository userRepository,
        AuthorityRepository authorityRepository
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        // Filtro de sincronización de usuarios
        Filter jwtUserSyncFilter = new JwtUserSyncFilter(userRepository, authorityRepository);

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz ->
                authz
                    // Permitimos la llamada interna de sync-user sin autenticar
                    .requestMatchers(mvc.pattern("/internal/sync-user")).permitAll()
                    // Endpoints públicos
                    .requestMatchers(mvc.pattern("/api/authenticate")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth-info")).permitAll()
                    .requestMatchers(mvc.pattern("/v3/api-docs/**")).permitAll()
                    .requestMatchers(mvc.pattern("/management/health")).permitAll()
                    .requestMatchers(mvc.pattern("/management/health/**")).permitAll()
                    .requestMatchers(mvc.pattern("/management/info")).permitAll()
                    .requestMatchers(mvc.pattern("/management/prometheus")).permitAll()
                    // Admin
                    .requestMatchers(mvc.pattern("/api/admin/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(mvc.pattern("/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                    // Resto de API requiere autenticación
                    .requestMatchers(mvc.pattern("/api/**")).authenticated()
            )
            // Registramos el filtro justo antes de procesar el pre-auth (Bearer token)
            .addFilterBefore(jwtUserSyncFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(authenticationConverter()))
            )
            .oauth2Client(withDefaults());

        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(
            new Converter<Jwt, Collection<GrantedAuthority>>() {
                @Override
                public Collection<GrantedAuthority> convert(Jwt jwt) {
                    return SecurityUtils.extractAuthorityFromClaims(jwt.getClaims());
                }
            }
        );
        conv.setPrincipalClaimName(PREFERRED_USERNAME);
        return conv;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        JwtDecoder decoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator =
            new AudienceValidator(jHipsterProperties.getSecurity().getOauth2().getAudience());
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
        return decoder;
    }
}
