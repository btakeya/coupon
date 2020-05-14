package com.btakeya.coupon.config

import com.btakeya.coupon.router.CouponHandler
import com.btakeya.coupon.router.HelloHandler
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

class CouponRouterConfigSpec {

    @RelaxedMockK
    private lateinit var helloHandler: HelloHandler

    @RelaxedMockK
    private lateinit var couponHandler: CouponHandler

    private lateinit var client: WebTestClient

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        client = WebTestClient.bindToRouterFunction(CouponRouterConfig().route(helloHandler, couponHandler)).build()
    }

    @Test
    fun `App hello 테스트`() {
        every { helloHandler.hello() } answers { ServerResponse.ok().body(Mono.just("hello"), String::class.java) }

        client.get()
            .uri("/hello")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith {
                assertThat(String(it?.responseBody ?: byteArrayOf())).isEqualTo("hello")
            }

        verify(exactly = 1) { helloHandler.hello() }
    }

    @Test
    fun `Coupon API hello 테스트`() {
        every { couponHandler.hello() } answers { ServerResponse.ok().body(Mono.just("coupon hello"), String::class.java) }

        client.get()
            .uri("/api/coupon/hello")
            .exchange()
            .expectStatus().isOk()
            .expectBody().consumeWith {
                assertThat(String(it?.responseBody ?: byteArrayOf())).isEqualTo("coupon hello")
            }

        verify(exactly = 1) { couponHandler.hello()}
    }
}