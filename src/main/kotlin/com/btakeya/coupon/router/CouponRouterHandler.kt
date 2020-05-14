package com.btakeya.coupon.router

import com.btakeya.coupon.domain.CouponDomainService
import com.btakeya.coupon.util.LogUtil
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CouponRouterHandler(val couponDomainService: CouponDomainService) {

    companion object: LogUtil()

    fun hello(): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(Mono.just(ResultDto("HELLO", "Coupon Hello")), ResultDto::class.java)
    }

    fun issue(param: CouponIssueParam): Mono<ServerResponse> {
        val result = couponDomainService.issue(param.count)
            .collectList()
            .map {
                ResultDto("ISSUE", "${it.size} coupon(s) issued successfully")
            }

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    data class CouponIssueParam(val count: Int)
}