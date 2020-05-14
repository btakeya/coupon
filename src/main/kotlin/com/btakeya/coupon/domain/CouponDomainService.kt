package com.btakeya.coupon.domain

import com.btakeya.coupon.repository.CouponRepository
import com.btakeya.coupon.repository.entity.Coupon
import com.btakeya.coupon.util.LogUtil
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
}

data class CouponDto (val code: String, val owner: String?, val issuedAt: LocalDateTime, val used: Boolean) {
    companion object {
        fun fromEntity(entity: Coupon): CouponDto {
            return CouponDto(entity.code, entity.owner, entity.issuedAt, entity.used)
        }
    }
}