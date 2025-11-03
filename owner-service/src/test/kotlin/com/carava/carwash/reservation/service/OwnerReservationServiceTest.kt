package com.carava.carwash.reservation.service

import com.carava.carwash.common.exception.ForbiddenException
import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import com.carava.carwash.domain.reservation.repository.ReservationRepository
import com.carava.carwash.domain.store.entity.Store
import com.carava.carwash.domain.store.repository.StoreRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.*

class OwnerReservationServiceTest {

    private lateinit var service: OwnerReservationService
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var storeRepository: StoreRepository

    @BeforeEach
    fun setup() {
        reservationRepository = mockk()
        storeRepository = mockk()

        service = OwnerReservationService(
            reservationRepository,
            storeRepository
        )
    }

    // ============================================
    // 권한 검증 테스트
    // ============================================

    @Test
    fun `점포 소유자가 아니면 ForbiddenException이 발생해야 한다`() {
        // Given
        val ownerId = 1L
        val storeId = 1L
        val store = mockStore(ownerMemberId = 999L) // 다른 소유자
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        
        // When & Then
        assertThrows<ForbiddenException> {
            service.getTodayReservations(ownerId, storeId)
        }
    }

    @Test
    fun `예약이 다른 점포 것이면 ForbiddenException이 발생해야 한다`() {
        // Given
        val ownerId = 1L
        val storeId = 1L
        val reservationId = 1L
        
        val store = mockStore(id = 1L, ownerMemberId = ownerId)
        val reservation = mockReservation(storeId = 999L) // 다른 점포
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)
        
        // When & Then
        assertThrows<ForbiddenException> {
            service.confirmReservation(ownerId, storeId, reservationId)
        }
    }

    // ============================================
    // 예약 승인 테스트
    // ============================================

    @Test
    fun `PENDING 예약은 승인할 수 있어야 한다`() {
        // Given
        val ownerId = 1L
        val storeId = 1L
        val reservationId = 1L
        
        val store = mockStore(ownerMemberId = ownerId)
        val reservation = mockReservation(
            id = reservationId,
            storeId = storeId,
            status = ReservationStatus.PENDING
        )
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { reservationRepository.findById(reservationId) } returns Optional.of(reservation)
        
        // When
        service.confirmReservation(ownerId, storeId, reservationId)
        
        // Then
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
        verify { reservation.updateStatus(ReservationStatus.CONFIRMED) }
    }

    @Test
    fun `CONFIRMED 예약을 승인하려 하면 예외가 발생해야 한다`() {
        // Given
        val store = mockStore()
        val reservation = mockReservation(status = ReservationStatus.CONFIRMED)
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        
        // When & Then
        assertThrows<IllegalStateException> {
            service.confirmReservation(1L, 1L, 1L)
        }
    }

    // ============================================
    // 예약 거절 테스트
    // ============================================

    @Test
    fun `PENDING 예약은 거절할 수 있어야 한다`() {
        // Given
        val store = mockStore()
        val reservation = mockReservation(status = ReservationStatus.PENDING)
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        
        // When
        service.rejectReservation(1L, 1L, 1L, "차량이 너무 큽니다")
        
        // Then
        assertEquals(ReservationStatus.REJECTED, reservation.status)
        assertEquals("차량이 너무 큽니다", reservation.rejectionReason)
    }

    // ============================================
    // 상태 전환 테스트
    // ============================================

    @Test
    fun `예약 상태 전환 흐름이 정상 작동해야 한다`() {
        // Given
        val store = mockStore()
        val reservation = mockReservation(status = ReservationStatus.PENDING)
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        
        // When & Then
        
        // 1. PENDING → CONFIRMED
        service.confirmReservation(1L, 1L, 1L)
        assertEquals(ReservationStatus.CONFIRMED, reservation.status)
        
        // 2. CONFIRMED → IN_PROGRESS
        service.startReservation(1L, 1L, 1L)
        assertEquals(ReservationStatus.IN_PROGRESS, reservation.status)
        
        // 3. IN_PROGRESS → COMPLETED
        service.completeReservation(1L, 1L, 1L)
        assertEquals(ReservationStatus.COMPLETED, reservation.status)
    }

    @Test
    fun `노쇼 처리는 예약 시간이 지난 CONFIRMED 예약만 가능해야 한다`() {
        // Given
        val store = mockStore()
        val pastReservation = mockReservation(
            status = ReservationStatus.CONFIRMED,
            reservationDateTime = LocalDateTime.now().minusHours(2) // 2시간 전
        )
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { reservationRepository.findById(any()) } returns Optional.of(pastReservation)
        every { pastReservation.isPastReservation(any()) } returns true
        
        // When
        service.markAsNoShow(1L, 1L, 1L)
        
        // Then
        assertEquals(ReservationStatus.NO_SHOW, pastReservation.status)
    }

    @Test
    fun `아직 시간이 안 지난 예약은 노쇼 처리할 수 없어야 한다`() {
        // Given
        val store = mockStore()
        val futureReservation = mockReservation(
            status = ReservationStatus.CONFIRMED,
            reservationDateTime = LocalDateTime.now().plusHours(2) // 2시간 후
        )
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { reservationRepository.findById(any()) } returns Optional.of(futureReservation)
        every { futureReservation.isPastReservation(any()) } returns false
        
        // When & Then
        assertThrows<IllegalStateException> {
            service.markAsNoShow(1L, 1L, 1L)
        }
    }

    // ============================================
    // Helper Methods
    // ============================================

    private fun mockStore(
        id: Long = 1L,
        ownerMemberId: Long = 1L
    ): Store {
        return mockk<Store>(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.ownerMemberId } returns ownerMemberId
            every { validateOwnership(any()) } answers {
                val requestMemberId = firstArg<Long>()
                if (requestMemberId != ownerMemberId) {
                    throw ForbiddenException("해당 가게에 대한 권한이 없습니다.")
                }
            }
        }
    }

    private fun mockReservation(
        id: Long = 1L,
        customerId: Long = 1L,
        storeId: Long = 1L,
        status: ReservationStatus = ReservationStatus.PENDING,
        reservationDateTime: LocalDateTime = LocalDateTime.now().plusDays(1)
    ): Reservation {
        return mockk<Reservation>(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.customerId } returns customerId
            every { this@mockk.storeId } returns storeId
            every { this@mockk.status } returns status
            every { this@mockk.reservationDateTime } returns reservationDateTime
            every { this@mockk.rejectionReason } returns null
            every { this@mockk.cancellationReason } returns null
            every { isCancellable() } returns status.isActive()
            every { isPastReservation(any()) } returns reservationDateTime.isBefore(LocalDateTime.now())
            every { updateStatus(any()) } answers {
                val newStatus = firstArg<ReservationStatus>()
                every { this@mockk.status } returns newStatus
            }
            every { updateStatusWithReason(any(), any()) } answers {
                val newStatus = firstArg<ReservationStatus>()
                val reason = secondArg<String?>()
                every { this@mockk.status } returns newStatus
                when (newStatus) {
                    ReservationStatus.CANCELLED -> every { this@mockk.cancellationReason } returns reason
                    ReservationStatus.REJECTED -> every { this@mockk.rejectionReason } returns reason
                    else -> {}
                }
            }
        }
    }
}
