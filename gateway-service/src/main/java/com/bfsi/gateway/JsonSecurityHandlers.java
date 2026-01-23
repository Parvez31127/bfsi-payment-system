package com.bfsi.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class JsonSecurityHandlers {

    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> write(exchange, HttpStatus.UNAUTHORIZED, "Unauthorized");
    }

    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> write(exchange, HttpStatus.FORBIDDEN, "Forbidden");
    }

    private Mono<Void> write(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String path = exchange.getRequest().getPath().value();

        String json = "{"
                + "\"timestamp\":\"" + Instant.now().toString() + "\","
                + "\"status\":" + status.value() + ","
                + "\"error\":\"" + message + "\","
                + "\"path\":\"" + path + "\""
                + "}";

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
        );
    }
}