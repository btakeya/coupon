package com.btakeya.coupon.domain

import com.btakeya.coupon.repository.CouponRepository
import com.btakeya.coupon.repository.entity.Coupon
import com.btakeya.coupon.util.LogUtil
import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Component
class CouponDomainService(val couponRepository: CouponRepository) {
    companion object: LogUtil()

    fun issue(count: Int): Flux<CouponDto> {
        if (count < 1) {
            return Flux.error(RuntimeException("최소 한 개 이상 발급할 수 있습니다."))
        }

        val newCoupons = (1..count).map { Coupon.newCoupon("Admin") }

        return Flux.defer {
            Flux.fromIterable(couponRepository.saveAll(newCoupons))
        }
                .retry(3)
                .subscribeOn(Schedulers.boundedElastic())
                .map { CouponDto.fromEntity(it) }
    }

    fun assign(code: String, userId: String): Mono<CouponDto> {
        return Mono.defer {
            Mono.justOrEmpty(couponRepository.findCouponByCodeAndOwnerIsNullAndUsedIsFalseAndExpiredIsFalse(code))
        }
            .switchIfEmpty(Mono.error(RuntimeException("유효한 쿠폰이 없습니다: ${code}")))
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.assign(userId) }
            .map { couponRepository.save(it) }
            .map { CouponDto.fromEntity(it) }
    }

    fun bulkAssign(codes: List<String>, userId: String): Flux<CouponDto> {
        return Flux.defer {
            Flux.fromIterable(couponRepository.findCouponsByCodeInAndUsedIsTrueOrOwnerIsNotNullOOrExpiredIsTrue(codes))
        }
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { Flux.error<Coupon>(RuntimeException("유효하지 않은 쿠폰이 포함되어 있습니다: ${codes}"))} // TODO: handle partial error (used, expired)
            .switchIfEmpty(Flux.fromIterable(couponRepository.findCouponsByCodeInAndUsedIsFalseAndOwnerIsNullAndExpiredIsFalse(codes)))
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.assign(userId) }
            .collectList()
            .map { couponRepository.saveAll(it) }
            .flatMapMany { Flux.fromIterable(it) }
            .map { CouponDto.fromEntity(it) }

    }

    fun list(includeUsed: Boolean, includeExpired: Boolean): Flux<CouponDto> {
        return Flux.defer {
            Flux.fromIterable(couponRepository.findCouponsByUsedAndExpired(includeUsed, includeExpired))
        }
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { CouponDto.fromEntity(it) }
    }

    fun use(code: String, userId: String): Mono<CouponDto> {
        return handleUsed(code, userId, true)
    }

    fun cancel(code: String, userId: String): Mono<CouponDto> {
        return handleUsed(code, userId, false)
    }

    private fun handleUsed(code: String, userId: String, used: Boolean): Mono<CouponDto> {
        return Mono.defer {
            Mono.justOrEmpty(couponRepository.findCouponByCodeAndOwnerAndUsedAndExpiredIsFalse(code, userId, used))
        }
            .switchIfEmpty(Mono.error(RuntimeException("사용자 ${userId}가 가진 유효한 쿠폰 ${code}을 찾을 수 없습니다.")))
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { if (used) it.use() else it.cancel() }
            .map { couponRepository.save(it) }
            .map { CouponDto.fromEntity(it) }
    }

    fun getExpiredCouponByDate(basisDate: LocalDate): Flux<CouponDto> {
        val expirationPeriods = Period.ofDays(3)
        return Flux.defer {
            val expectedExpirationDate = basisDate.minus(expirationPeriods)
            val expectedIssuedAtStart = expectedExpirationDate.atTime(0, 0, 0)
            val expectedIssuedAtEnd = expectedExpirationDate.atTime(23, 59, 59)
            Flux.fromIterable(couponRepository.findCouponsByExpiredIsTrueAndIssuedAtBetween(expectedIssuedAtStart, expectedIssuedAtEnd))
        }
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { CouponDto.fromEntity(it) }
    }
}

data class CouponDto (val code: String, val owner: String?, @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")val issuedAt: LocalDateTime, val used: Boolean) {
    companion object {
        fun fromEntity(entity: Coupon): CouponDto {
            return CouponDto(entity.code, entity.owner, entity.issuedAt, entity.used)
        }
    }
}