package com.carava.carwash.reservation.service

import com.carava.carwash.common.exception.ConflictException
import com.carava.carwash.common.exception.ForbiddenException
import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationMenu
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import com.carava.carwash.domain.reservation.repository.ReservationRepository
import com.carava.carwash.domain.store.entity.*
import com.carava.carwash.domain.store.repository.HolidayRepository
import com.carava.carwash.domain.store.repository.MenuRepository
import com.carava.carwash.domain.store.repository.OperatingHourRepository
import com.carava.carwash.domain.store.repository.StoreRepository
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

class CustomerReservationServiceTest {

    private lateinit var service: CustomerReservationService
    private lateinit var reservationRepository: ReservationRepository
    private lateinit var storeRepository: StoreRepository
    private lateinit var menuRepository: MenuRepository
    private lateinit var operatingHourRepository: OperatingHourRepository
    private lateinit var holidayRepository: HolidayRepository

    @BeforeEach
    fun setup() {
        reservationRepository = mockk()
        storeRepository = mockk()
        menuRepository = mockk()
        operatingHourRepository = mockk()
        holidayRepository = mockk()

        service = CustomerReservationService(
            reservationRepository,
            storeRepository,
            menuRepository,
            operatingHourRepository,
            holidayRepository
        )
    }

    // ============================================
    // 예약 가능 시간 조회 테스트
    // ============================================

    @Test
    fun `예약이 없으면 모든 슬롯이 available이어야 한다`() {
        // Given
        val storeId = 1L
        val date = LocalDate.now().plusDays(7)  // 미래 날짜
        
        val store = mockStore(hourlyCapacity = 2, slotDuration = 30)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(12, 0)
        )
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { holidayRepository.existsByStoreIdAndDate(storeId, date) } returns false
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(storeId, date.dayOfWeek) } returns operatingHour  // 동적 계산
        every { reservationRepository.findByStoreIdAndDateAndStatusIn(any(), any(), any()) } returns emptyList()
        
        // When
        val result = service.getAvailableTimeSlots(storeId, date)
        
        // Then
        assertEquals(6, result.size) // 09:00, 09:30, 10:00, 10:30, 11:00, 11:30
        assertTrue(result.all { it.isAvailable })
        assertTrue(result.all { it.availableCount == 2 })
        assertEquals(LocalTime.of(9, 0), result.first().time)
        assertEquals(LocalTime.of(11, 30), result.last().time)
    }

    @Test
    fun `특정 시간에 예약이 있으면 availableCount가 감소해야 한다`() {
        // Given
        val storeId = 1L
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore(hourlyCapacity = 2)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(14, 0),
            closeTime = LocalTime.of(15, 0)
        )
        
        // 14:00에 예약 1건 있음
        val existingReservation = mockReservation(
            reservationDateTime = LocalDateTime.of(date.year, date.month, date.dayOfMonth, 14, 0)
        )
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { holidayRepository.existsByStoreIdAndDate(storeId, date) } returns false
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { reservationRepository.findByStoreIdAndDateAndStatusIn(any(), any(), any()) } returns listOf(existingReservation)
        
        // When
        val result = service.getAvailableTimeSlots(storeId, date)
        
        // Then
        val slot14 = result.find { it.time == LocalTime.of(14, 0) }
        assertNotNull(slot14)
        assertEquals(1, slot14!!.availableCount) // 2 - 1 = 1
        assertTrue(slot14.isAvailable)
        
        val slot1430 = result.find { it.time == LocalTime.of(14, 30) }
        assertEquals(2, slot1430!!.availableCount) // 예약 없음
    }

    @Test
    fun `용량이 꽉 차면 isAvailable이 false여야 한다`() {
        // Given
        val storeId = 1L
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore(hourlyCapacity = 2)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(14, 0),
            closeTime = LocalTime.of(15, 0)
        )
        
        // 14:00에 예약 2건 (꽉참)
        val reservation1 = mockReservation(reservationDateTime = LocalDateTime.of(date.year, date.month, date.dayOfMonth, 14, 0))
        val reservation2 = mockReservation(reservationDateTime = LocalDateTime.of(date.year, date.month, date.dayOfMonth, 14, 0))
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { holidayRepository.existsByStoreIdAndDate(storeId, date) } returns false
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { reservationRepository.findByStoreIdAndDateAndStatusIn(any(), any(), any()) } returns listOf(reservation1, reservation2)
        
        // When
        val result = service.getAvailableTimeSlots(storeId, date)
        
        // Then
        val slot14 = result.find { it.time == LocalTime.of(14, 0) }
        assertNotNull(slot14)
        assertEquals(0, slot14!!.availableCount)
        assertFalse(slot14.isAvailable) // 마감!
    }

    @Test
    fun `휴일이면 빈 리스트를 반환해야 한다`() {
        // Given
        val storeId = 1L
        val date = LocalDate.of(2025, 10, 25)
        
        val store = mockStore()
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { holidayRepository.existsByStoreIdAndDate(storeId, date) } returns true
        
        // When
        val result = service.getAvailableTimeSlots(storeId, date)
        
        // Then
        assertTrue(result.isEmpty())
    }

    // ============================================
    // 예약 생성 테스트
    // ============================================

    @Test
    fun `정상적인 예약 생성 시 PENDING 상태로 저장되어야 한다`() {
        // Given
        val customerId = 1L
        val storeId = 1L
        val carId = 1L
        val date = LocalDate.now().plusDays(7)  // 미래 날짜!
        val time = LocalTime.of(14, 0)
        
        val store = mockStore(hourlyCapacity = 2)
        val menu1 = mockMenu(id = 1L, price = BigDecimal(20000), duration = 30)
        val menu2 = mockMenu(id = 2L, price = BigDecimal(25000), duration = 30)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(18, 0)
        )
        
        every { storeRepository.findById(storeId) } returns Optional.of(store)
        every { menuRepository.findAllById(listOf(1L, 2L)) } returns listOf(menu1, menu2)
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { holidayRepository.existsByStoreIdAndDate(any(), any()) } returns false
        every { reservationRepository.countByStoreIdAndDateTimeAndStatusIn(any(), any(), any()) } returns 0
        every { reservationRepository.save(any()) } answers { firstArg() }
        
        // When
        val result = service.createReservation(
            customerId, storeId, carId, date, time, listOf(1L, 2L)
        )
        
        // Then
        assertEquals(ReservationStatus.PENDING, result.status)
        assertEquals(BigDecimal(45000), result.totalAmount)
        assertEquals(60, result.estimatedDuration)
        verify { reservationRepository.save(any()) }
    }

    @Test
    fun `용량 초과 시 ConflictException이 발생해야 한다`() {
        // Given
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore(hourlyCapacity = 2)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(18, 0)
        )
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { menuRepository.findAllById(any()) } returns listOf(mockMenu())
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { holidayRepository.existsByStoreIdAndDate(any(), any()) } returns false
        every { reservationRepository.countByStoreIdAndDateTimeAndStatusIn(any(), any(), any()) } returns 2 // 이미 2건!
        
        // When & Then
        assertThrows<ConflictException> {
            service.createReservation(
                1L, 1L, 1L,
                date,
                LocalTime.of(14, 0),
                listOf(1L)
            )
        }
    }

    @Test
    fun `과거 시간 예약 시 IllegalArgumentException이 발생해야 한다`() {
        // Given
        val store = mockStore()
        val operatingHour = mockOperatingHour()
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { menuRepository.findAllById(any()) } returns listOf(mockMenu())
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            service.createReservation(
                1L, 1L, 1L,
                LocalDate.of(2020, 1, 1), // 과거
                LocalTime.of(14, 0),
                listOf(1L)
            )
        }
    }

    @Test
    fun `휴일 예약 시 IllegalArgumentException이 발생해야 한다`() {
        // Given
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore()
        val operatingHour = mockOperatingHour()
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { menuRepository.findAllById(any()) } returns listOf(mockMenu())
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { holidayRepository.existsByStoreIdAndDate(any(), any()) } returns true // 휴일!
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            service.createReservation(
                1L, 1L, 1L,
                date,
                LocalTime.of(14, 0),
                listOf(1L)
            )
        }
        
        assertEquals("해당 날짜는 휴일입니다.", exception.message)
    }

    // ============================================
    // 예약 취소 테스트
    // ============================================

    @Test
    fun `본인 예약 취소는 성공해야 한다`() {
        // Given
        val customerId = 1L
        val reservation = mockReservation(
            id = 1L,
            customerId = customerId,
            status = ReservationStatus.PENDING
        )
        
        every { reservationRepository.findById(1L) } returns Optional.of(reservation)
        
        // When
        service.cancelReservation(1L, customerId, "개인 사정")
        
        // Then
        assertEquals(ReservationStatus.CANCELLED, reservation.status)
        assertEquals("개인 사정", reservation.cancellationReason)
    }

    @Test
    fun `다른 사람 예약 취소 시 ForbiddenException이 발생해야 한다`() {
        // Given
        val reservation = mockReservation(customerId = 1L)
        
        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        
        // When & Then
        assertThrows<ForbiddenException> {
            service.cancelReservation(1L, 999L, null) // 다른 사람
        }
    }

    @Test
    fun `COMPLETED 상태 예약은 취소할 수 없어야 한다`() {
        // Given
        val reservation = mockReservation(
            customerId = 1L,
            status = ReservationStatus.COMPLETED
        )
        
        every { reservationRepository.findById(any()) } returns Optional.of(reservation)
        
        // When & Then
        assertThrows<IllegalStateException> {
            service.cancelReservation(1L, 1L, null)
        }
    }

    // ============================================
    // 예약 가능 여부 체크 테스트 ✨
    // ============================================

    @Test
    fun `예약 가능한 시간이면 available이 true여야 한다`() {
        // Given
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore(hourlyCapacity = 2)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(18, 0)
        )
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { holidayRepository.existsByStoreIdAndDate(any(), any()) } returns false
        every { reservationRepository.countByStoreIdAndDateTimeAndStatusIn(any(), any(), any()) } returns 1 // 1건 있음
        
        // When
        val result = service.checkAvailability(
            1L,
            date,
            LocalTime.of(14, 0)
        )
        
        // Then
        assertTrue(result.available)
        assertEquals(1, result.availableCount) // 2 - 1 = 1
        assertEquals("예약 가능합니다.", result.message)
    }

    @Test
    fun `예약 마감 시간이면 available이 false여야 한다`() {
        // Given
        val date = LocalDate.now().plusDays(7)
        
        val store = mockStore(hourlyCapacity = 2)
        val operatingHour = mockOperatingHour(
            openTime = LocalTime.of(9, 0),
            closeTime = LocalTime.of(18, 0)
        )
        
        every { storeRepository.findById(any()) } returns Optional.of(store)
        every { operatingHourRepository.findByStoreIdAndDayOfWeek(any(), date.dayOfWeek) } returns operatingHour
        every { holidayRepository.existsByStoreIdAndDate(any(), any()) } returns false
        every { reservationRepository.countByStoreIdAndDateTimeAndStatusIn(any(), any(), any()) } returns 2 // 2건 (꽉참)
        
        // When
        val result = service.checkAvailability(
            1L,
            date,
            LocalTime.of(14, 0)
        )
        
        // Then
        assertFalse(result.available)
        assertEquals(0, result.availableCount)
    }

    // ============================================
    // Helper Methods (Mock 객체 생성)
    // ============================================

    private fun mockStore(
        id: Long = 1L,
        hourlyCapacity: Int = 2,
        slotDuration: Int = 30
    ): Store {
        return mockk<Store>(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.hourlyCapacity } returns hourlyCapacity
            every { this@mockk.slotDuration } returns slotDuration
            every { canAcceptMoreReservations(any()) } answers {
                val count = firstArg<Int>()
                count < hourlyCapacity
            }
            every { getAvailableCapacity(any()) } answers {
                val count = firstArg<Int>()
                (hourlyCapacity - count).coerceAtLeast(0)
            }
            every { validateOwnership(any()) } just Runs  // ✅ 추가
        }
    }

    private fun mockOperatingHour(
        openTime: LocalTime = LocalTime.of(9, 0),
        closeTime: LocalTime = LocalTime.of(18, 0),
        isOpen: Boolean = true
    ): OperatingHour {
        return mockk<OperatingHour>(relaxed = true) {
            every { this@mockk.openTime } returns openTime
            every { this@mockk.closeTime } returns closeTime
            every { this@mockk.isOpen } returns isOpen
        }
    }

    private fun mockMenu(
        id: Long = 1L,
        price: BigDecimal = BigDecimal(20000),
        duration: Int = 30
    ): Menu {
        return mockk<Menu>(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.price } returns price
            every { this@mockk.duration } returns duration
            every { this@mockk.name } returns "테스트메뉴"
        }
    }

    private fun mockReservation(
        id: Long = 1L,
        customerId: Long = 1L,
        storeId: Long = 1L,
        reservationDateTime: LocalDateTime = LocalDateTime.now().plusDays(1),
        status: ReservationStatus = ReservationStatus.PENDING
    ): Reservation {
        return mockk<Reservation>(relaxed = true) {
            every { this@mockk.id } returns id
            every { this@mockk.customerId } returns customerId
            every { this@mockk.storeId } returns storeId
            every { this@mockk.reservationDateTime } returns reservationDateTime
            every { this@mockk.status } returns status
            every { getReservationTime() } returns reservationDateTime.toLocalTime()
            every { isCancellable() } returns status.isActive()
            every { updateStatus(any()) } just Runs
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
