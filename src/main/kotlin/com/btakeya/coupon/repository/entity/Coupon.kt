package com.btakeya.coupon.repository.entity

import java.time.LocalDateTime
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "coupon")
data class Coupon(
    @Id
    @GeneratedValue
    val seq: Long?,

    val code: String,

    var owner: String?,

    val issuedBy: String?,
    val issuedAt: LocalDateTime,

    var used: Boolean,
    var expired: Boolean
) {
    companion object {
        fun newCoupon(): Coupon {
            return newCoupon(null)
        }

        fun newCoupon(issuer: String?): Coupon {
            return Coupon(null, UUID.randomUUID().toString(), null, issuer, LocalDateTime.now(), false, false)
        }
    }

    fun use(): Coupon {
        this.used = true
        return this
    }

    fun cancel(): Coupon {
        this.used = false
        return this
    }

    fun assign(userId: String): Coupon {
        this.owner = userId
        return this
    }

    fun expire(): Coupon {
        this.expired = true
        return this
    }
}