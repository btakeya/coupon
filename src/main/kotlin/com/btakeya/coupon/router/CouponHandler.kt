package com.btakeya.coupon.router

import com.btakeya.coupon.domain.CouponDomainService
import com.btakeya.coupon.util.LogUtil
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class CouponHandler(val couponDomainService: CouponDomainService) {

    companion object: LogUtil()

    fun hello(): Mono<ServerResponse> {
        return ServerResponse.ok()
            .body(Mono.just(ResultDto("HELLO", "Coupon Hello", "Hello")), ResultDto::class.java)
    }

    fun issue(param: CouponIssueParam): Mono<ServerResponse> {
        val result = couponDomainService.issue(param.count)
            .collectList()
            .map {
                ResultDto("ISSUE", "${it.size} coupon(s) issued successfully", it)
            }

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    fun assign(param: CouponAssignParam): Mono<ServerResponse> {
        val result = couponDomainService.assign(param.code, param.userId)
            .map { ResultDto("ASSIGN", "Coupon '${it.code}' is assigned to ${it.owner} successfully", it) }
            .switchIfEmpty(Mono.error(RuntimeException("Coupon not found: ${param.code}")))

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    fun bulkAssign(param: CouponBulkAssignParam): Mono<ServerResponse> {
        val result = couponDomainService.bulkAssign(param.codes, param.userId)
            .collectList()
            .map { ResultDto("BULK_ASSIGN", "${it.size} Coupons are assigned to ${param.userId} successfully", it)}
            .switchIfEmpty(Mono.error(RuntimeException("Coupon not found: ${param.codes}"))) // TODO: handle partial error

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    fun list(includeUsed: Boolean): Mono<ServerResponse> {
        val result = couponDomainService.list(includeUsed)
            .collectList()
            .map { ResultDto("LIST", "Coupons w/${if (!includeUsed) "o" else ""} include used", it)}

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    data class CouponIssueParam(val count: Int)
    data class CouponAssignParam(val code: String, val userId: String)
    data class CouponBulkAssignParam(val codes: List<String>, val userId: String)

    class ResultDto(val code: String, val msg: String, val data: Any)
}