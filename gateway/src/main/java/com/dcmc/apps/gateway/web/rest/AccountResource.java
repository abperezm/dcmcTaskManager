package com.dcmc.apps.gateway.web.rest;

import com.dcmc.apps.gateway.client.UserSyncClient;
import com.dcmc.apps.gateway.security.SecurityUtils;
import com.dcmc.apps.gateway.service.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import java.net.URI;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResource.class);
    private final UserSyncClient userSyncClient;

    public AccountResource(UserSyncClient userSyncClient) {
        this.userSyncClient = userSyncClient;
    }

    private static class AccountResourceException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public AccountResourceException(String message) {
            super(message);
        }
    }

    /**
     * GET  /account : retorna el usuario actual y lo sincroniza en el microservicio.
     */
    @GetMapping("/account")
    public Mono<UserVM> getAccount(Principal principal) {
        if (!(principal instanceof AbstractAuthenticationToken token)) {
            throw new AccountResourceException("User could not be found");
        }

        UserDTO userDTO = buildUserDTO(token.getName(), extractAttributes(token));

        if (token instanceof JwtAuthenticationToken jwtToken) {
            // API client con JWT: enviamos el token al microservicio
            String bearer = jwtToken.getToken().getTokenValue();
            return userSyncClient
                .syncUser(userDTO, bearer)
                .thenReturn(toVM(jwtToken.getName(), jwtToken.getAuthorities(), extractAttributes(jwtToken)));

        } else if (token instanceof OAuth2AuthenticationToken oauth2) {
            // Login UI: no hay que pasar token (se asume sesión)
            return userSyncClient
                .syncUser(userDTO)
                .thenReturn(toVM(oauth2.getName(), oauth2.getAuthorities(), extractAttributes(oauth2)));

        } else {
            throw new AccountResourceException("Unsupported authentication token");
        }
    }

    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity
            .status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT)
            .build();
    }

    private Map<String, Object> extractAttributes(AbstractAuthenticationToken auth) {
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt.getTokenAttributes();
        } else if (auth instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttributes();
        }
        return Collections.emptyMap();
    }

    private UserDTO buildUserDTO(String login, Map<String, Object> attrs) {
        UserDTO dto = new UserDTO();
        dto.setLogin(login);
        dto.setEmail((String) attrs.get("email"));
        dto.setFirstName((String) attrs.get("given_name"));
        dto.setLastName((String) attrs.get("family_name"));
        dto.setActivated(true);
        dto.setLangKey("en");

        Object rolesRaw = attrs.get("roles");
        Set<String> roles = new HashSet<>();
        if (rolesRaw instanceof Collection<?> col) {
            col.forEach(r -> roles.add(r.toString()));
        }
        dto.setRoles(roles);

        return dto;
    }

    private UserVM toVM(String login, Collection<? extends GrantedAuthority> auths, Map<String, Object> details) {
        return new UserVM(
            login,
            auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
            SecurityUtils.extractDetailsFromTokenAttributes(details)
        );
    }

    private static class UserVM {
        private final String login;
        private final Set<String> authorities;
        private final Map<String, Object> details;

        UserVM(String login, Set<String> authorities, Map<String, Object> details) {
            this.login = login;
            this.authorities = authorities;
            this.details = details;
        }

        public String getLogin() { return login; }
        public Set<String> getAuthorities() { return authorities; }

        @JsonAnyGetter
        public Map<String, Object> getDetails() { return details; }

        public boolean isActivated() { return true; }
    }
}
