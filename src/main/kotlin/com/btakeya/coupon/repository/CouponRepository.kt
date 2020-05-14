package com.btakeya.coupon.repository

import com.btakeya.coupon.repository.entity.Coupon
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface CouponRepository : JpaRepository<Coupon, Long> {
    fun findCouponByCodeAndOwnerIsNullAndUsedIsFalse(code: String): Coupon?
    @Query("SELECT c FROM Coupon c WHERE c.code IN ?1 AND (c.used = true OR c.owner IS NOT NULL)")
    fun findCouponsByCodeInAndUsedIsTrueOrOwnerIsNotNull(code: List<String>): List<Coupon>
    fun findCouponsByCodeInAndUsedIsFalseAndOwnerIsNull(code: List<String>): List<Coupon>
    fun findCouponsByUsedIsFalse(): List<Coupon>
}