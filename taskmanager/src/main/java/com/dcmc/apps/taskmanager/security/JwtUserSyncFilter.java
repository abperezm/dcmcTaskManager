package com.dcmc.apps.taskmanager.security;

import com.dcmc.apps.taskmanager.domain.Authority;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.repository.AuthorityRepository;
import com.dcmc.apps.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtUserSyncFilter extends GenericFilterBean {

    private final Logger log = LoggerFactory.getLogger(JwtUserSyncFilter.class);

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public JwtUserSyncFilter(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Override
    @Transactional
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String path = httpReq.getRequestURI();
        // No re-sincronices si ya lo haces vía /internal/sync-user
        if (path.startsWith("/internal/sync-user")) {
            chain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();
            String login = jwt.getClaimAsString("preferred_username");
            if (login != null) {
                log.debug("JwtUserSyncFilter procesando login: {}", login);
                // Si no existe un usuario con este login, crea uno nuevo...
                if (userRepository.findOneByLogin(login).isEmpty()) {
                    User newUser = new User();
                    // <— aquí asignamos el id (PK) igual al login
                    newUser.setId(login);
                    newUser.setLogin(login);
                    newUser.setEmail(jwt.getClaimAsString("email"));
                    newUser.setFirstName(jwt.getClaimAsString("given_name"));
                    newUser.setLastName(jwt.getClaimAsString("family_name"));
                    newUser.setActivated(true);
                    newUser.setLangKey("en");

                    // extrae roles desde el claim “roles”
                    Set<String> roles = SecurityUtils.extractAuthorityFromClaims(jwt.getClaims())
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                    // crea/recupera cada Authority y asígnalas al usuario
                    Set<Authority> authorities = roles.stream()
                        .map(role -> authorityRepository.findById(role).orElseGet(() -> {
                            Authority a = new Authority();
                            a.setName(role);
                            return authorityRepository.save(a);
                        }))
                        .collect(Collectors.toSet());
                    newUser.setAuthorities(authorities);

                    userRepository.save(newUser);
                    log.info("✅ Usuario sincronizado desde JWT: {}", login);
                }
            }
        }

        chain.doFilter(request, response);
    }
}
