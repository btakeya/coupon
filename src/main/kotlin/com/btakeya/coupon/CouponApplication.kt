package com.btakeya.coupon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
class CouponApplication

fun main(args: Array<String>) {
    runApplication<CouponApplication>(*args)
}
