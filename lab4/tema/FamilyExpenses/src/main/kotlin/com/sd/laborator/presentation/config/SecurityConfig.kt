package com.sd.laborator.presentation.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.GET, "/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/**").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/**").permitAll()
                    .requestMatchers(HttpMethod.PATCH, "/**").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/**").permitAll()
                    .anyRequest().authenticated()
            }

        return http.build()
    }
}