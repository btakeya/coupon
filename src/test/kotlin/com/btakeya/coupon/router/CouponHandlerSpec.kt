package com.btakeya.coupon.router

import com.btakeya.coupon.config.CouponRouterConfig
import com.btakeya.coupon.domain.CouponDomainService
import com.btakeya.coupon.router.CouponHandler.ResultDto
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient


class CouponHandlerSpec {

    @RelaxedMockK
    private lateinit var couponDomainService: CouponDomainService

    @RelaxedMockK
    private lateinit var helloHandler: HelloHandler

    private lateinit var couponHandler: CouponHandler

    private lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        couponHandler = CouponHandler(couponDomainService)
        client = WebTestClient.bindToRouterFunction(CouponRouterConfig().route(helloHandler, couponHandler)).build()
    }

    @Disabled // assert는 모두 통과하는데 consumeWith에서 NPE 발생 - kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)
    @Test
    fun `hello 호출 성공`() {
        client.get()
            .uri("/api/coupon/hello")
            .exchange()
            .expectStatus().isOk()
            .expectBody(ResultDto::class.java)
            .consumeWith<Nothing> {
                val responseBody = (it?.responseBody ?: fail("Null Response"))
                assertThat(responseBody.code).isEqualTo("HELLO")
                assertThat(responseBody.msg).isEqualTo("Coupon Hello")
                assertThat(responseBody.data).isEqualTo("Hello")
            }
    }
}