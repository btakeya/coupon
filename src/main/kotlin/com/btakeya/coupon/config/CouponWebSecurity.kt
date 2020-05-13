package com.btakeya.coupon.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class CouponWebSecurity {
    @Bean
    fun configure(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain {
        return httpSecurity
            .exceptionHandling()
            .and()
            .authorizeExchange()
            .pathMatchers("/hello").permitAll()
            .pathMatchers("/api/coupon/**").permitAll()
            .anyExchange().denyAll()
            .and()
            .build()
    }
}