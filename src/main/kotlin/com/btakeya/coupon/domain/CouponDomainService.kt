package com.btakeya.coupon.domain

import com.btakeya.coupon.repository.CouponRepository
import com.btakeya.coupon.repository.entity.Coupon
import com.btakeya.coupon.util.LogUtil
import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDateTime

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
            Mono.justOrEmpty(couponRepository.findCouponByCodeAndOwnerIsNullAndUsedIsFalse(code))
        }
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.assign(userId) }
            .map { couponRepository.save(it) }
            .map { CouponDto.fromEntity(it) }
    }

    fun bulkAssign(codes: List<String>, userId: String): Flux<CouponDto> {
        return Flux.defer {
            Flux.fromIterable(couponRepository.findCouponsByCodeInAndUsedIsTrueOrOwnerIsNotNull(codes))
        }
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap { Flux.error<Coupon>(RuntimeException("이미 발급됐거나 사용한 쿠폰이 포함되어 있습니다: ${codes}"))} // TODO: handle partial error
            .switchIfEmpty(Flux.fromIterable(couponRepository.findCouponsByCodeInAndUsedIsFalseAndOwnerIsNull(codes)))
            .retry(3)
            .subscribeOn(Schedulers.boundedElastic())
            .map { it.assign(userId) }
            .collectList()
            .map { couponRepository.saveAll(it) }
            .flatMapMany { Flux.fromIterable(it) }
            .map { CouponDto.fromEntity(it) }

    }

    fun list(includeUsed: Boolean): Flux<CouponDto> {
        return Flux.defer {
            Flux.fromIterable(if (includeUsed) couponRepository.findAll() else couponRepository.findCouponsByUsedIsFalse())
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