package com.company.quanlyluong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.GrantedAuthority;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/employee/**").hasRole("EMPLOYEE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login") // Đảm bảo khớp với action của form HTML
                .usernameParameter("username") // Đảm bảo khớp với name="username" trong HTML
                .passwordParameter("password") // Đảm bảo khớp với name="password" trong HTML
                .successHandler((request, response, authentication) -> {
                    for (GrantedAuthority auth : authentication.getAuthorities()) {
                        if (auth.getAuthority().equals("ROLE_ADMIN")) {
                            response.sendRedirect("/admin/dashboard");
                            return;
                        }
                    }
                    response.sendRedirect("/employee/dashboard");
                })
                .permitAll()
            )
            .logout(logout -> logout.permitAll());
        
        return http.build();
    }
}