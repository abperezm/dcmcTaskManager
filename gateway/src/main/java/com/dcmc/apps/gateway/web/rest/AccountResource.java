package com.dcmc.apps.gateway.web.rest;

import com.dcmc.apps.gateway.client.UserSyncClient;
import com.dcmc.apps.gateway.security.SecurityUtils;
import com.dcmc.apps.gateway.service.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
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

        private AccountResourceException(String message) {
            super(message);
        }
    }

    @GetMapping("/account")
    public Mono<UserVM> getAccount(Principal principal) {
        if (principal instanceof JwtAuthenticationToken jwtToken) {
            Map<String, Object> attributes = jwtToken.getTokenAttributes();
            UserDTO userDTO = buildUserDTO(jwtToken.getName(), attributes);
            String rawToken = jwtToken.getToken().getTokenValue();

            return userSyncClient.syncUser(userDTO, rawToken)
                .thenReturn(new UserVM(
                    jwtToken.getName(),
                    jwtToken.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                    SecurityUtils.extractDetailsFromTokenAttributes(attributes)
                ));
        } else if (principal instanceof AbstractAuthenticationToken token) {
            Map<String, Object> attributes = extractAttributes(token);
            UserDTO userDTO = buildUserDTO(token.getName(), attributes);

            return Mono.just(new UserVM(
                token.getName(),
                token.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()),
                SecurityUtils.extractDetailsFromTokenAttributes(attributes)
            ));
        } else {
            throw new AccountResourceException("User could not be found");
        }
    }

    @GetMapping("/authenticate")
    public ResponseEntity<Void> isAuthenticated(Principal principal) {
        LOG.debug("REST request to check if the current user is authenticated");
        return ResponseEntity.status(principal == null ? HttpStatus.UNAUTHORIZED : HttpStatus.NO_CONTENT).build();
    }

    private Map<String, Object> extractAttributes(AbstractAuthenticationToken authToken) {
        if (authToken instanceof JwtAuthenticationToken jwt) {
            return jwt.getTokenAttributes();
        } else if (authToken instanceof OAuth2AuthenticationToken oauth2) {
            return oauth2.getPrincipal().getAttributes();
        } else {
            throw new IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!");
        }
    }

    private UserDTO buildUserDTO(String login, Map<String, Object> attributes) {
        UserDTO dto = new UserDTO();
        dto.setLogin(login);
        dto.setEmail((String) attributes.get("email"));
        dto.setFirstName((String) attributes.get("given_name"));
        dto.setLastName((String) attributes.get("family_name"));
        dto.setActivated(true);
        dto.setLangKey("en");

        Object rolesAttr = attributes.get("roles");
        Set<String> roles = new HashSet<>();
        if (rolesAttr instanceof Collection<?> list) {
            roles = list.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        }

        dto.setRoles(roles);
        return dto;
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

        public boolean isActivated() {
            return true;
        }

        public Set<String> getAuthorities() {
            return authorities;
        }

        public String getLogin() {
            return login;
        }

        @JsonAnyGetter
        public Map<String, Object> getDetails() {
            return details;
        }
    }
}
