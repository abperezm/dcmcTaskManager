package com.dcmc.apps.gateway.client;

import com.dcmc.apps.gateway.service.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserSyncClient {

    private final WebClient webClient;

    public UserSyncClient(@Value("${application.microservices.taskmanager-url}") String taskManagerUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(taskManagerUrl)
            .build();
    }

    /** Llamada simple (OAuth2 / UI login) */
    public Mono<Void> syncUser(UserDTO userDTO) {
        return webClient.post()
            .uri("/internal/sync-user")
            .bodyValue(userDTO)
            .retrieve()
            .bodyToMono(Void.class);
    }

    /** Llamada con token en cabecera (JWT clients) */
    public Mono<Void> syncUser(UserDTO userDTO, String bearerToken) {
        return webClient.post()
            .uri("/internal/sync-user")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
            .bodyValue(userDTO)
            .retrieve()
            .bodyToMono(Void.class);
    }
}
