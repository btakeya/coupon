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
        if (param.count < 1) {
            return Mono.error(RuntimeException("최소 한 개 이상 발급할 수 있습니다."))
        }

        val result = couponDomainService.issue(param.count)
            .collectList()
            .map {
                ResultDto("ISSUE", "${it.size} coupon(s) issued successfully", it)
            }

        return ServerResponse.ok()
            .body(result, ResultDto::class.java)
    }

    private fun isValidCouponCode(code: String): Boolean {
        val uuidRegex = """[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}""".toRegex()
        return uuidRegex.matchEntire(code) != null
    }

    fun assign(param: CouponAssignParam): Mono<ServerResponse> {
        if (!isValidCouponCode(param.code)) { // UUID length, ex: 3e22847e-b963-4e01-be3b-d4b346c2e99d
            return Mono.error(RuntimeException("올바르지 않은 쿠폰 번호입니다."))
        }

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