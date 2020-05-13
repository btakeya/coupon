package com.btakeya.coupon.router

import com.btakeya.coupon.util.LogUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CouponRouterHandler {

    companion object: LogUtil()

    fun hello(): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(Mono.just("coupon hello"), String::class.java)
    }
}