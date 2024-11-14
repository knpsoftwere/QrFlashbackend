package org.qrflash.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebSecurity
public class SecurityConfig{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // Вимикаємо CSRF для простоти
                .authorizeHttpRequests()
                .requestMatchers("/auth/register", "/auth/login").permitAll() // Дозволяємо доступ до реєстрації та логіну без авторизації
                .anyRequest().authenticated() // Вимагаємо аутентифікацію для інших маршрутів
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // Вимикаємо сесії для REST API
        return http.build();
    }
}
