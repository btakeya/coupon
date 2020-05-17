package com.btakeya.coupon.domain

import com.btakeya.coupon.repository.CouponRepository
import com.btakeya.coupon.util.LogUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class CouponExpirationDomainService(val couponRepository: CouponRepository) {

    companion object: LogUtil()

//    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00시
    @Scheduled(cron = "0 * * * * *")
    fun couponExpirationCheck() = runBlocking {
        async {
            log.info("Expiration check...")
            val now = LocalDate.now().atTime(0, 0, 0)
            val expirationDate = now.minusDays(3) // 3일 전 00:00:00 부터
            val expiredCouponList = couponRepository.findCouponsByUsedIsFalseAndIssuedAtIsBeforeAndExpiredIsFalse(expirationDate)
            expiredCouponList.map { c -> c.expired = true }
            couponRepository.saveAll(expiredCouponList)

            if (!expiredCouponList.isEmpty()) {
                log.info("${expiredCouponList.size}개의 쿠폰이 만료처리 되었습니다.")
            }
        }
    }
}

