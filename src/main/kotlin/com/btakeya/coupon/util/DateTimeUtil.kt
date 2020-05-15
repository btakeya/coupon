package com.btakeya.coupon.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun LocalDateTime.toJavaDate(): Date {
    return Date.from(this.atZone(ZoneId.systemDefault()).toInstant())
}

fun Date.toLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneId.systemDefault())
}
