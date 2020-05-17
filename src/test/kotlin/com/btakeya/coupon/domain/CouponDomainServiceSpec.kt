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

    @Test
    fun `유효한 쿠폰을 사용자에게 할당 성공`() {
        // TODO
    }

    @Test
    fun `유효하지 않는 쿠폰을 사용자에게 할당 실패`() {
        // TODO
    }

    @Test
    fun `유효한 쿠폰들을 사용자에게 할당 성공`() {
        // TODO
    }

    @Test
    fun `유효하지 않은 쿠폰들을 사용자에게 할당 실패`() {
        // TODO
    }

    @Test
    fun `쿠폰 목록 조회 성공`() {
        // TODO
    }

    @Test
    fun `유효한 쿠폰 사용 성공`() {
        // TODO
    }

    @Test
    fun `이미 사용한 쿠폰 사용 실패`() {
        // TODO
    }

    @Test
    fun `이미 만료된 쿠폰 사용 실패`() {
        // TODO
    }

    @Test
    fun `존재하지 않는 쿠폰 사용 실패`() {
        // TODO
    }

    @Test
    fun `유효한 쿠폰 사용 취소 성공`() {
        // TODO
    }

    @Test
    fun `사용하지 않은 쿠폰 사용 취소 실패`() {
        // TODO
    }

    @Test
    fun `이미 만료된 쿠폰 사용 취소 실패`() {
        // TODO
    }

    @Test
    fun `존재하지 않는 쿠폰 사용 취소 실패`() {
        // TODO
    }

    @Test
    fun `특정 날짜로 만료된 쿠폰 목록 조회 성공`() {
        // TODO
    }
}