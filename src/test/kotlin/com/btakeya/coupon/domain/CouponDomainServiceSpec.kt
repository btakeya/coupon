package com.btakeya.coupon.domain

import com.btakeya.coupon.repository.CouponRepository
import com.btakeya.coupon.repository.entity.Coupon
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier

class CouponDomainServiceSpec {
    @MockK
    private lateinit var couponRepository: CouponRepository

    private lateinit var couponDomainService: CouponDomainService
    private val dummyCoupon = Coupon.newCoupon()

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        couponDomainService = CouponDomainService(couponRepository)
    }

    @Test
    fun `쿠폰 1개 발행 성공`() {
        val expectedCouponList = listOf(dummyCoupon)
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { expectedCouponList }

        StepVerifier.create(couponDomainService.issue(1))
            .expectNext(CouponDto.fromEntity(dummyCoupon))
            .verifyComplete()

        val slot = slot<List<Coupon>>()
        verify(exactly = 1) { couponRepository.saveAll(capture(slot)) }
        assertThat(slot.captured.size).isEqualTo(1)
    }

    @Test
    fun `쿠폰 10개 발행 성공`() {
        val couponCount = 10
        val expectedCouponList = (1..couponCount).map { dummyCoupon }
        every { couponRepository.saveAll(any<List<Coupon>>()) } answers { expectedCouponList }

        var verifier: StepVerifier.Step<CouponDto> = StepVerifier.create(couponDomainService.issue(couponCount))
        for (time in 1..couponCount) {
            verifier = verifier.expectNext(CouponDto.fromEntity(dummyCoupon))
        }

        verifier.verifyComplete()

        val slot = slot<List<Coupon>>()
        verify(exactly = 1) { couponRepository.saveAll(capture(slot)) }
        assertThat(slot.captured.size).isEqualTo(couponCount)
    }

    @Test
    fun `쿠폰 0개 발행 실패`() {
        val couponCount = 0
        StepVerifier.create(couponDomainService.issue(couponCount))
            .expectErrorMatches { it is RuntimeException && it.message == "최소 한 개 이상 발급할 수 있습니다." }
            .verify()

        verify(exactly = 0) { couponRepository.saveAll(any<List<Coupon>>())}
    }
}