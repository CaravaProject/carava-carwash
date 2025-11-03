package com.carava.carwash.domain.reservation.entity

import com.carava.carwash.domain.store.entity.Store
import com.carava.carwash.domain.store.entity.StoreCategory
import com.carava.carwash.domain.store.entity.StoreStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ReservationTest {

    // ============================================
    // getReservationDate/Time 테스트
    // ============================================

    @Test
    fun `getReservationDate는 날짜만 반환해야 한다`() {
        // Given
        val reservation = createReservation(
            reservationDateTime = LocalDateTime.of(2025, 10, 21, 14, 30)
        )
        
        // When
        val date = reservation.getReservationDate()
        
        // Then
        assertEquals(LocalDate.of(2025, 10, 21), date)
    }

    @Test
    fun `getReservationTime는 시간만 반환해야 한다`() {
        // Given
        val reservation = createReservation(
            reservationDateTime = LocalDateTime.of(2025, 10, 21, 14, 30)
        )
        
        // When
        val time = reservation.getReservationTime()
        
        // Then
        assertEquals(LocalTime.of(14, 30), time)
    }

    // ============================================
    // getEndDateTime 테스트
    // ============================================

    @Test
    fun `getEndDateTime은 시작시간 + 예상소요시간을 반환해야 한다`() {
        // Given
        val reservation = createReservation(
            reservationDateTime = LocalDateTime.of(2025, 10, 21, 14, 0),
            estimatedDuration = 60
        )
        
        // When
        val endDateTime = reservation.getEndDateTime()
        
        // Then
        assertEquals(LocalDateTime.of(2025, 10, 21, 15, 0), endDateTime)
    }

    @Test
    fun `30분 소요시 정확히 계산되어야 한다`() {
        // Given
        val reservation = createReservation(
            reservationDateTime = LocalDateTime.of(2025, 10, 21, 14, 0),
            estimatedDuration = 30
        )
        
        // When
        val endDateTime = reservation.getEndDateTime()
        
        // Then
        assertEquals(LocalDateTime.of(2025, 10, 21, 14, 30), endDateTime)
    }

    // ============================================
    // isPastReservation 테스트
    // ============================================

    @Test
    fun `현재 시간보다 과거면 true를 반환해야 한다`() {
        // Given
        val pastReservation = createReservation(
            reservationDateTime = LocalDateTime.now().minusHours(2)
        )
        
        // When
        val isPast = pastReservation.isPastReservation()
        
        // Then
        assertTrue(isPast)
    }

    @Test
    fun `현재 시간보다 미래면 false를 반환해야 한다`() {
        // Given
        val futureReservation = createReservation(
            reservationDateTime = LocalDateTime.now().plusHours(2)
        )
        
        // When
        val isPast = futureReservation.isPastReservation()
        
        // Then
        assertFalse(isPast)
    }

    // ============================================
    // isCancellable 테스트
    // ============================================

    @Test
    fun `PENDING 상태는 취소 가능해야 한다`() {
        // Given
        val reservation = createReservation(status = ReservationStatus.PENDING)
        
        // When & Then
        assertTrue(reservation.isCancellable())
    }

    @Test
    fun `CONFIRMED 상태는 취소 가능해야 한다`() {
        // Given
        val reservation = createReservation(status = ReservationStatus.CONFIRMED)
        
        // When & Then
        assertTrue(reservation.isCancellable())
    }

    @Test
    fun `IN_PROGRESS 상태는 취소 가능해야 한다`() {
        // Given
        val reservation = createReservation(status = ReservationStatus.IN_PROGRESS)
        
        // When & Then
        assertTrue(reservation.isCancellable())
    }

    @Test
    fun `COMPLETED 상태는 취소 불가능해야 한다`() {
        // Given
        val reservation = createReservation(status = ReservationStatus.COMPLETED)
        
        // When & Then
        assertFalse(reservation.isCancellable())
    }

    @Test
    fun `CANCELLED 상태는 취소 불가능해야 한다`() {
        // Given
        val reservation = createReservation(status = ReservationStatus.CANCELLED)
        
        // When & Then
        assertFalse(reservation.isCancellable())
    }

    // ============================================
    // updateStatusWithReason 테스트
    // ============================================

    @Test
    fun `예약 거절 시 rejectionReason이 저장되어야 한다`() {
        // Given
        val reservation = createReservation()
        
        // When
        reservation.updateStatusWithReason(
            ReservationStatus.REJECTED,
            "차량이 너무 큽니다"
        )
        
        // Then
        assertEquals(ReservationStatus.REJECTED, reservation.status)
        assertEquals("차량이 너무 큽니다", reservation.rejectionReason)
    }

    @Test
    fun `예약 취소 시 cancellationReason이 저장되어야 한다`() {
        // Given
        val reservation = createReservation()
        
        // When
        reservation.updateStatusWithReason(
            ReservationStatus.CANCELLED,
            "개인 사정"
        )
        
        // Then
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
        assertEquals("개인 사정", reservation.cancellationReason)
    }

    // ============================================
    // Helper Methods
    // ============================================

    private fun createReservation(
        id: Long = 1L,
        customerId: Long = 1L,
        storeId: Long = 1L,
        carId: Long = 1L,
        reservationDateTime: LocalDateTime = LocalDateTime.now().plusDays(1),
        estimatedDuration: Int = 60,
        status: ReservationStatus = ReservationStatus.PENDING,
        totalAmount: BigDecimal = BigDecimal(45000),
        finalAmount: BigDecimal = BigDecimal(45000)
    ): Reservation {
        return Reservation(
            id = id,
            customerId = customerId,
            storeId = storeId,
            carId = carId,
            reservationDateTime = reservationDateTime,
            estimatedDuration = estimatedDuration,
            status = status,
            totalAmount = totalAmount,
            finalAmount = finalAmount
        )
    }
}
