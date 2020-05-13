package com.btakeya.coupon.util

import org.slf4j.LoggerFactory

open class LogUtil {

    @Suppress("JAVA_CLASS_ON_COMPANION")
    val log = LoggerFactory.getLogger(javaClass.enclosingClass)
}