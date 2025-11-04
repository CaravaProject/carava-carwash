package com.carava.carwash.reservation.service

import com.carava.carwash.common.exception.ConflictException
import com.carava.carwash.common.exception.ForbiddenException
import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationMenu
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import com.carava.carwash.domain.reservation.repository.ReservationRepository
import com.carava.carwash.domain.store.repository.HolidayRepository
import com.carava.carwash.domain.store.repository.MenuRepository
import com.carava.carwash.domain.store.repository.OperatingHourRepository
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.reservation.dto.AvailabilityCheckResult
import com.carava.carwash.reservation.dto.TimeSlotDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Service
@Transactional
class CustomerReservationService(
    private val reservationRepository: ReservationRepository,
    private val storeRepository: StoreRepository,
    private val menuRepository: MenuRepository,
    private val operatingHourRepository: OperatingHourRepository,
    private val holidayRepository: HolidayRepository
) {

    /**
     * 예약 가능한 시간 슬롯 조회 (동적 계산)
     */
    @Transactional(readOnly = true)
    fun getAvailableTimeSlots(
        storeId: Long,
        date: LocalDate
    ): List<TimeSlotDto> {
        
        // 1. 점포 조회
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        
        // 2. 휴일 체크
        if (holidayRepository.existsByStoreIdAndDate(storeId, date)) {
            return emptyList()
        }
        
        // 3. 영업시간 조회
        val dayOfWeek = date.dayOfWeek
        val operatingHour = operatingHourRepository
            .findByStoreIdAndDayOfWeek(storeId, dayOfWeek)
            ?: return emptyList()
        
        if (!operatingHour.isOpen) {
            return emptyList()
        }
        
        // 4. 모든 시간 슬롯 생성
        val allTimeSlots = generateTimeSlots(
            start = operatingHour.openTime!!,
            end = operatingHour.closeTime!!,
            interval = store.slotDuration
        )
        
        // 5. 해당 날짜의 활성 예약 조회
        val activeStatuses = listOf(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.IN_PROGRESS
        )
        val reservations = reservationRepository.findByStoreIdAndDateAndStatusIn(
            storeId = storeId,
            date = date,
            statuses = activeStatuses
        )
        
        // 6. 시간대별 예약 수 카운트
        val reservationCountByTime = reservations
            .groupBy { it.getReservationTime() }
            .mapValues { it.value.size }
        
        // 7. 각 슬롯의 가용성 계산
        return allTimeSlots.map { slotTime ->
            val currentCount = reservationCountByTime[slotTime] ?: 0
            val availableCount = store.getAvailableCapacity(currentCount)
            
            TimeSlotDto(
                time = slotTime,
                availableCount = availableCount,
                totalCapacity = store.hourlyCapacity,
                isAvailable = availableCount > 0
            )
        }
    }
    
    /**
     * 예약 생성
     */
    fun createReservation(
        customerId: Long,
        storeId: Long,
        carId: Long,
        reservationDate: LocalDate,
        reservationTime: LocalTime,
        menuIds: List<Long>,
        customerRequest: String? = null
    ): Reservation {
        
        // 1. 점포 조회
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        
        // 2. 메뉴 조회
        val menus = menuRepository.findAllById(menuIds)
        if (menus.size != menuIds.size) {
            throw NotFoundException("일부 메뉴를 찾을 수 없습니다.")
        }
        
        // 3. 예약 시간 생성
        val reservationDateTime = LocalDateTime.of(reservationDate, reservationTime)
        
        // 4. 예약 가능 여부 체크
        validateReservationAvailability(store, reservationDateTime)
        
        // 5. 금액 계산
        val totalAmount = menus.sumOf { it.price }
        val estimatedDuration = menus.sumOf { it.duration }
        
        // 6. 예약 생성
        val reservation = Reservation(
            customerId = customerId,
            storeId = storeId,
            carId = carId,
            reservationDateTime = reservationDateTime,
            estimatedDuration = estimatedDuration,
            totalAmount = totalAmount,
            finalAmount = totalAmount,
            customerRequest = customerRequest,
            status = ReservationStatus.PENDING
        )
        
        // 7. 예약 메뉴 추가
        menus.forEach { menu ->
            val reservationMenu = ReservationMenu.fromMenu(menu)
            reservation.addMenu(reservationMenu)
        }
        
        // 8. 저장
        return reservationRepository.save(reservation)
    }
    
    /**
     * 예약 가능 여부 검증
     */
    private fun validateReservationAvailability(
        store: com.carava.carwash.domain.store.entity.Store,
        reservationDateTime: LocalDateTime
    ) {
        // 1. 과거 시간 체크
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("과거 시간으로는 예약할 수 없습니다.")
        }
        
        // 2. 영업시간 체크
        val dayOfWeek = reservationDateTime.dayOfWeek
        val operatingHour = operatingHourRepository
            .findByStoreIdAndDayOfWeek(store.id, dayOfWeek)
            ?: throw IllegalArgumentException("해당 요일은 영업하지 않습니다.")
        
        if (!operatingHour.isOpen) {
            throw IllegalArgumentException("해당 요일은 영업하지 않습니다.")
        }
        
        val requestTime = reservationDateTime.toLocalTime()
        if (requestTime.isBefore(operatingHour.openTime) || 
            requestTime.isAfter(operatingHour.closeTime!!.minusMinutes(30))) {
            throw IllegalArgumentException("영업시간 내에만 예약 가능합니다.")
        }
        
        // 3. 휴일 체크
        val date = reservationDateTime.toLocalDate()
        if (holidayRepository.existsByStoreIdAndDate(store.id, date)) {
            throw IllegalArgumentException("해당 날짜는 휴일입니다.")
        }
        
        // 4. 용량 체크 (핵심!)
        val activeStatuses = listOf(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.IN_PROGRESS
        )
        
        val currentCount = reservationRepository.countByStoreIdAndDateTimeAndStatusIn(
            storeId = store.id,
            dateTime = reservationDateTime,
            statuses = activeStatuses
        )
        
        if (!store.canAcceptMoreReservations(currentCount)) {
            throw ConflictException(
                "해당 시간은 예약이 마감되었습니다. " +
                "다른 시간을 선택해주세요."
            )
        }
    }
    
    /**
     * 예약 취소
     */
    fun cancelReservation(
        reservationId: Long,
        customerId: Long,
        cancellationReason: String? = null
    ) {
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            NotFoundException("예약을 찾을 수 없습니다.")
        }
        
        // 권한 체크
        if (reservation.customerId != customerId) {
            throw ForbiddenException("본인의 예약만 취소할 수 있습니다.")
        }
        
        // 취소 가능 상태 체크
        if (!reservation.isCancellable()) {
            throw IllegalStateException("취소할 수 없는 예약 상태입니다.")
        }
        
        // 취소 처리
        reservation.updateStatusWithReason(
            ReservationStatus.CANCELLED,
            cancellationReason
        )
    }
    
    /**
     * 내 예약 목록 조회 (활성)
     */
    @Transactional(readOnly = true)
    fun getMyReservations(customerId: Long): List<Reservation> {
        return reservationRepository.findActiveReservationsByCustomerId(customerId)
    }
    
    /**
     * ✨ 예약 히스토리 조회 (완료/취소/거절/노쇼)
     */
    @Transactional(readOnly = true)
    fun getReservationHistory(
        customerId: Long,
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<Reservation> {
        return reservationRepository.findHistoryReservationsByCustomerId(customerId, pageable)
    }
    
    /**
     * ✨ 특정 점포에서의 내 예약 히스토리
     */
    @Transactional(readOnly = true)
    fun getMyReservationsAtStore(
        customerId: Long,
        storeId: Long
    ): List<Reservation> {
        return reservationRepository.findByCustomerIdAndStoreIdOrderByReservationDateTimeDesc(
            customerId, storeId
        )
    }
    
    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    fun getReservationDetail(
        reservationId: Long,
        customerId: Long
    ): Reservation {
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            NotFoundException("예약을 찾을 수 없습니다.")
        }
        
        if (reservation.customerId != customerId) {
            throw ForbiddenException("본인의 예약만 조회할 수 있습니다.")
        }
        
        return reservation
    }
    
    /**
     * ✨ 예약 수정 (시간/메뉴 변경)
     */
    fun updateReservation(
        reservationId: Long,
        customerId: Long,
        newReservationDate: LocalDate?,
        newReservationTime: LocalTime?,
        newMenuIds: List<Long>?
    ): Reservation {
        // 1. 기존 예약 조회
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            NotFoundException("예약을 찾을 수 없습니다.")
        }
        
        // 2. 권한 체크
        if (reservation.customerId != customerId) {
            throw ForbiddenException("본인의 예약만 수정할 수 있습니다.")
        }
        
        // 3. 상태 체크 (PENDING만 수정 가능)
        if (reservation.status != ReservationStatus.PENDING) {
            throw IllegalStateException("대기 중인 예약만 수정할 수 있습니다.")
        }
        
        // 4. 시간 변경이 있으면 새로 생성 (기존 취소 후 재생성)
        if (newReservationDate != null && newReservationTime != null) {
            // 기존 예약 취소
            reservation.updateStatus(ReservationStatus.CANCELLED)
            
            // 새 예약 생성
            return createReservation(
                customerId = customerId,
                storeId = reservation.storeId,
                carId = reservation.carId,
                reservationDate = newReservationDate,
                reservationTime = newReservationTime,
                menuIds = newMenuIds ?: reservation.reservationMenus.map { it.menuId },
                customerRequest = reservation.customerRequest
            )
        }
        
        // 5. 메뉴만 변경
        if (newMenuIds != null && newMenuIds != reservation.reservationMenus.map { it.menuId }) {
            // 기존 예약 취소
            reservation.updateStatus(ReservationStatus.CANCELLED)
            
            // 같은 시간으로 재생성
            return createReservation(
                customerId = customerId,
                storeId = reservation.storeId,
                carId = reservation.carId,
                reservationDate = reservation.getReservationDate(),
                reservationTime = reservation.getReservationTime(),
                menuIds = newMenuIds,
                customerRequest = reservation.customerRequest
            )
        }
        
        return reservation
    }
    
    /**
     * ✨ 예약 가능 여부 체크 (예약 전 확인)
     */
    @Transactional(readOnly = true)
    fun checkAvailability(
        storeId: Long,
        reservationDate: LocalDate,
        reservationTime: LocalTime
    ): AvailabilityCheckResult {
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        
        val reservationDateTime = LocalDateTime.of(reservationDate, reservationTime)
        
        try {
            validateReservationAvailability(store, reservationDateTime)
            
            // 가능하면 남은 자리 수 계산
            val activeStatuses = listOf(
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED,
                ReservationStatus.IN_PROGRESS
            )
            val currentCount = reservationRepository.countByStoreIdAndDateTimeAndStatusIn(
                storeId = store.id,
                dateTime = reservationDateTime,
                statuses = activeStatuses
            )
            
            return AvailabilityCheckResult(
                available = true,
                availableCount = store.getAvailableCapacity(currentCount),
                totalCapacity = store.hourlyCapacity,
                message = "예약 가능합니다."
            )
        } catch (e: Exception) {
            return AvailabilityCheckResult(
                available = false,
                availableCount = 0,
                totalCapacity = store.hourlyCapacity,
                message = e.message ?: "예약이 불가능합니다."
            )
        }
    }
    
    // ============================================
    // Private Helper Methods
    // ============================================
    
    /**
     * 시간 슬롯 생성
     */
    private fun generateTimeSlots(
        start: LocalTime,
        end: LocalTime,
        interval: Int
    ): List<LocalTime> {
        val slots = mutableListOf<LocalTime>()
        var current = start
        
        while (current.isBefore(end)) {
            slots.add(current)
            current = current.plusMinutes(interval.toLong())
        }
        
        return slots
    }
}
