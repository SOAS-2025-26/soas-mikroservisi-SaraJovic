package com.currencyapp.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String USERS_SERVICE_URL = "http://localhost:8770";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(WebClient.Builder webClientBuilder) {
        WebClient webClient = webClientBuilder.baseUrl(USERS_SERVICE_URL).build();

        return username -> webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users/email").queryParam("email", username).build())
                .retrieve()
                .bodyToMono(RemoteUserDto.class)
                .map(dto -> (UserDetails) User.withUsername(dto.email())
                        .password(dto.password())
                        .roles(dto.role())
                        .build())
                .onErrorResume(WebClientResponseException.class, ex -> ex.getStatusCode().is4xxClientError()
                        ? Mono.empty()
                        : Mono.error(ex));
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(httpBasicSpec -> {})
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/currency-exchange/**", "/crypto-exchange/**").permitAll()
                        .pathMatchers("/currency-conversion/**", "/trade-service/**").hasRole("USER")
                        .pathMatchers("/bank-accounts/**", "/crypto-wallets/**").hasAnyRole("ADMIN", "OWNER")
                        .pathMatchers(HttpMethod.DELETE, "/users/**").hasRole("OWNER")
                        .pathMatchers(HttpMethod.GET, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .pathMatchers(HttpMethod.POST, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .anyExchange().authenticated()
                );

        return http.build();
    }

    private record RemoteUserDto(String email, String password, String role) {}
}
