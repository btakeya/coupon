package com.btakeya.coupon.router

import com.btakeya.coupon.config.CouponRouterConfig
import com.btakeya.coupon.domain.CouponDomainService
import com.btakeya.coupon.domain.CouponDto
import com.btakeya.coupon.router.CouponHandler.CouponAssignParam
import com.btakeya.coupon.router.CouponHandler.CouponIssueParam
import com.btakeya.coupon.router.CouponHandler.ResultDto
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CouponHandlerSpec {

    @RelaxedMockK
    private lateinit var couponDomainService: CouponDomainService

    @RelaxedMockK
    private lateinit var helloHandler: HelloHandler

    private lateinit var couponHandler: CouponHandler

    private lateinit var client: WebTestClient

    private val dummyCoupon: CouponDto = CouponDto("3e22847e-b963-4e01-be3b-d4b346c2e99d", "OWNER", LocalDateTime.now(), false)

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        couponHandler = CouponHandler(couponDomainService)
        client = WebTestClient.bindToRouterFunction(CouponRouterConfig().route(helloHandler, couponHandler)).build()
//        client.mutate()
//            .responseTimeout(Duration.ofMillis(30000))
//            .build()
    }

    @Disabled("assert는 모두 통과하는데 consumeWith에서 NPE 발생 - kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)")
    @Test
    fun `hello 호출 성공`() {
        client.get()
            .uri("/api/coupon/hello")
            .exchange()
            .expectStatus().isOk
            .expectBody(ResultDto::class.java)
            .consumeWith<Nothing> {
                val responseBody = (it?.responseBody ?: fail("Null Response"))
                assertThat(responseBody.code).isEqualTo("HELLO")
                assertThat(responseBody.msg).isEqualTo("Coupon Hello")
                assertThat(responseBody.data).isEqualTo("Hello")
            }
    }

    @Disabled("assert는 모두 통과하는데 consumeWith에서 NPE 발생 - kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)")
    @Test
    fun `쿠폰 1개 발급 요청 처리 성공`() {
        val desiredCouponCount = 1

        val dummyResult = (1..desiredCouponCount).map { dummyCoupon }
        every { couponDomainService.issue(any()) } answers { Flux.fromIterable(dummyResult) }

        client.post()
            .uri("/api/coupon/issue")
            .bodyValue(CouponIssueParam(desiredCouponCount))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResultDto::class.java)
            .consumeWith<Nothing> {
                val responseBody = (it?.responseBody ?: fail("Null Response"))
                assertThat(responseBody.code).isEqualTo("ISSUE")
                assertThat(responseBody.msg).isEqualTo("1 coupon(s) issued successfully")
                assertThat(responseBody.data).matches { d ->
                    d is List<*> && d.size == desiredCouponCount // should be List<CouponDto>
                }
            }

        verify(exactly = 1) { couponDomainService.issue(desiredCouponCount) }
    }

    @Disabled("assert는 모두 통과하는데 consumeWith에서 NPE 발생 - kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)")
    @Test
    fun `쿠폰 100개 발급 요청 처리 성공`() {
        val desiredCouponCount = 100

        val dummyResult = (1..desiredCouponCount).map { dummyCoupon }
        every { couponDomainService.issue(any()) } answers { Flux.fromIterable(dummyResult) }

        client.post()
            .uri("/api/coupon/issue")
            .bodyValue(CouponIssueParam(desiredCouponCount))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResultDto::class.java)
            .consumeWith<Nothing> {
                val responseBody = (it?.responseBody ?: fail("Null Response"))
                assertThat(responseBody.code).isEqualTo("ISSUE")
                assertThat(responseBody.msg).isEqualTo("100 coupon(s) issued successfully")
                assertThat(responseBody.data).matches { d ->
                    d is List<*> && d.size == 100 // should be List<CouponDto>
                }
            }

        verify(exactly = 1) { couponDomainService.issue(desiredCouponCount) }
    }

    @Test
    fun `쿠폰 0개 발급 요청 처리 실패`() {
        val desiredCouponCount = 0

        client.post()
            .uri("/api/coupon/issue")
            .bodyValue(CouponIssueParam(desiredCouponCount))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().is5xxServerError
//            .expectBody(ResultDto::class.java) TODO: Error handling

        verify(exactly = 0) { couponDomainService.issue(any()) }
    }

    @Disabled("assert는 모두 통과하는데 consumeWith에서 NPE 발생 - kotlin의 type inference 문제로 보임 (https://youtrack.jetbrains.com/issue/KT-5464#comment=27-2262874)")
    @Test
    fun `존재하는 쿠폰을 사용자에게 할당 성공`() {
        val couponCode = dummyCoupon.code
        val userId = dummyCoupon.owner!!
        every { couponDomainService.assign(any(), any()) } answers { Mono.just(dummyCoupon) }

        client.put()
            .uri("/api/coupon/assign")
            .bodyValue(CouponAssignParam(couponCode, userId))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().isOk
            .expectBody(ResultDto::class.java)
            .consumeWith<Nothing> {
                val responseBody = (it?.responseBody ?: fail("Null Response"))
                assertThat(responseBody.code).isEqualTo("ASSIGN")
                assertThat(responseBody.msg).isEqualTo("Coupon '${couponCode}' is assigned to ${userId} successfully")
                assertThat(responseBody.data).matches { d ->
                    val m = d as LinkedHashMap<String, Any>
                    m.get("code") == dummyCoupon.code &&
                    m.get("owner") == dummyCoupon.owner &&
                    m.get("issuedAt") == dummyCoupon.issuedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) &&
                    m.get("used") == dummyCoupon.used
                }
            }

        verify(exactly = 1) { couponDomainService.assign(couponCode, userId) }
    }

    @Test
    fun `실제로 존재하지 않거나 사용한 쿠폰을 사용자에게 할당 실패`() {
        val usedCouponCode = "f0e8c639-70bd-46d7-91be-40a9623b9885"
        val userId = "user"
        every { couponDomainService.assign(any(), any()) } answers { Mono.error(RuntimeException("이미 발급됐거나 사용한 쿠폰입니다")) }

        client.put()
            .uri("/api/coupon/assign")
            .bodyValue(CouponAssignParam(usedCouponCode, userId))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchange()
            .expectStatus().is5xxServerError
        //            .expectBody(ResultDto::class.java) TODO: Error handling
    }

    @Test
    fun `실제로 존재하는 쿠폰들을 사용자에게 할당 성공`() {
        // TODO
    }

    @Test
    fun `실제로 존재하지 않거나 사용한 쿠폰들을 사용자에게 할당 실패`() {
        // TODO
    }

    @Test
    fun `쿠폰 목록 조회 성공`() {
        // TODO
    }
}