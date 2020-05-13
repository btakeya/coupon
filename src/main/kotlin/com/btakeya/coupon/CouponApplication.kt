package com.btakeya.coupon

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
@EnableWebFlux
class CouponApplication

fun main(args: Array<String>) {
    runApplication<CouponApplication>(*args)
}
