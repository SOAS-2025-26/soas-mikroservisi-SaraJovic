package com.currencyapp.apigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class AuthHeaderFilter implements GlobalFilter, Ordered {

    private static final Map<String, String> USERNAME_TO_EMAIL = Map.of(
            "owner", "owner@app.com",
            "admin", "admin@app.com",
            "user", "user@app.com"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .flatMap(authentication -> propagateHeaders(exchange, chain, authentication))
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> propagateHeaders(ServerWebExchange exchange, GatewayFilterChain chain, Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return chain.filter(exchange);
        }

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(authority -> authority.replace("ROLE_", ""))
                .orElse("");
        String email = USERNAME_TO_EMAIL.getOrDefault(username, "");

        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Role", role)
                .header("X-User-Email", email)
                .build();

        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
