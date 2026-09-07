package com.currencyapp.usersservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    private static final String ROLE_HEADER = "X-User-Role";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/email").permitAll()
                        .requestMatchers("/users/validate").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").hasAnyRole("OWNER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new HeaderRoleAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static class HeaderRoleAuthenticationFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String role = request.getHeader(ROLE_HEADER);

            if (role != null && !role.isBlank()) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(role, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        }
    }
}
