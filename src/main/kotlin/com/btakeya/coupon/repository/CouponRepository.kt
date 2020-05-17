package com.btakeya.coupon.repository

import com.btakeya.coupon.repository.entity.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface CouponRepository : JpaRepository<Coupon, Long> {
    fun findCouponByCodeAndOwnerIsNullAndUsedIsFalseAndExpiredIsFalse(code: String): Coupon?
    fun findCouponByCodeAndOwnerAndUsedAndExpiredIsFalse(code: String, owner: String, used: Boolean): Coupon?
    @Query("SELECT c FROM Coupon c WHERE c.code IN ?1 AND (c.used = true OR c.owner IS NOT NULL OR c.expired = true)")
    fun findCouponsByCodeInAndUsedIsTrueOrOwnerIsNotNullOOrExpiredIsTrue(code: List<String>): List<Coupon>
    fun findCouponsByCodeInAndUsedIsFalseAndOwnerIsNullAndExpiredIsFalse(code: List<String>): List<Coupon>
    fun findCouponsByUsedAndExpired(used: Boolean, expired: Boolean): List<Coupon>
    fun findCouponsByUsedIsFalseAndIssuedAtIsBeforeAndExpiredIsFalse(issuedAt: LocalDateTime): List<Coupon>
    fun findCouponsByExpiredIsTrueAndIssuedAtBetween(start: LocalDateTime, end: LocalDateTime): List<Coupon>
}