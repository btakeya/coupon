package com.btakeya.coupon.router

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class HelloHandler {
    fun hello(): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(Mono.just("hello"), String::class.java)
    }
}