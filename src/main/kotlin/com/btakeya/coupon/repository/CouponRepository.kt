package com.btakeya.coupon.repository

import com.btakeya.coupon.repository.entity.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRepository : JpaRepository<Coupon, Long> {
}