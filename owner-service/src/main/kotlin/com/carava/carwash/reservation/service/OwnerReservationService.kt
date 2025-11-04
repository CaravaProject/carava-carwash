package com.carava.carwash.reservation.service

import com.carava.carwash.common.exception.ForbiddenException
import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import com.carava.carwash.domain.reservation.repository.ReservationRepository
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.reservation.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class OwnerReservationService(
    private val reservationRepository: ReservationRepository,
    private val storeRepository: StoreRepository
) {

    /**
     * 점포의 예약 목록 조회 (날짜별)
     */
    @Transactional(readOnly = true)
    fun getReservationsByDate(
        ownerId: Long,
        storeId: Long,
        date: LocalDate
    ): List<Reservation> {
        // 점포 소유권 확인
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        store.validateOwnership(ownerId)
        
        // 해당 날짜의 모든 예약 조회
        return reservationRepository.findByStoreIdAndDate(storeId, date)
    }
    
    /**
     * 점포의 예약 목록 조회 (날짜 + 상태별)
     */
    @Transactional(readOnly = true)
    fun getReservationsByDateAndStatus(
        ownerId: Long,
        storeId: Long,
        date: LocalDate,
        statuses: List<ReservationStatus>
    ): List<Reservation> {
        // 점포 소유권 확인
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        store.validateOwnership(ownerId)
        
        return reservationRepository.findByStoreIdAndDateAndStatusIn(storeId, date, statuses)
    }
    
    /**
     * 오늘의 예약 목록
     */
    @Transactional(readOnly = true)
    fun getTodayReservations(
        ownerId: Long,
        storeId: Long
    ): List<Reservation> {
        return getReservationsByDate(ownerId, storeId, LocalDate.now())
    }
    
    /**
     * 대기 중인 예약 목록
     */
    @Transactional(readOnly = true)
    fun getPendingReservations(
        ownerId: Long,
        storeId: Long,
        date: LocalDate
    ): List<Reservation> {
        return getReservationsByDateAndStatus(
            ownerId,
            storeId,
            date,
            listOf(ReservationStatus.PENDING)
        )
    }
    
    /**
     * 예약 승인
     */
    fun confirmReservation(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 상태 체크
        if (reservation.status != ReservationStatus.PENDING) {
            throw IllegalStateException("대기 중인 예약만 승인할 수 있습니다.")
        }
        
        // 승인 처리
        reservation.updateStatus(ReservationStatus.CONFIRMED)
        
        // TODO: FCM 알림 발송 (Phase 2)
        // notificationService.sendReservationConfirmed(reservation)
    }
    
    /**
     * 예약 거절
     */
    fun rejectReservation(
        ownerId: Long,
        storeId: Long,
        reservationId: Long,
        rejectionReason: String?
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 상태 체크
        if (reservation.status != ReservationStatus.PENDING) {
            throw IllegalStateException("대기 중인 예약만 거절할 수 있습니다.")
        }
        
        // 거절 처리
        reservation.updateStatusWithReason(ReservationStatus.REJECTED, rejectionReason)
        
        // TODO: FCM 알림 발송 (Phase 2)
        // notificationService.sendReservationRejected(reservation)
    }
    
    /**
     * 예약 시작 (진행중으로 변경)
     */
    fun startReservation(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 상태 체크
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("확정된 예약만 시작할 수 있습니다.")
        }
        
        // 진행중으로 변경
        reservation.updateStatus(ReservationStatus.IN_PROGRESS)
    }
    
    /**
     * 예약 완료
     */
    fun completeReservation(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 상태 체크
        if (reservation.status != ReservationStatus.IN_PROGRESS) {
            throw IllegalStateException("진행 중인 예약만 완료할 수 있습니다.")
        }
        
        // 완료 처리
        reservation.updateStatus(ReservationStatus.COMPLETED)
        
        // TODO: 리뷰 요청 알림 (Phase 2)
        // notificationService.requestReview(reservation)
    }
    
    /**
     * 노쇼 처리
     */
    fun markAsNoShow(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 상태 체크
        if (reservation.status != ReservationStatus.CONFIRMED) {
            throw IllegalStateException("확정된 예약만 노쇼 처리할 수 있습니다.")
        }
        
        // 과거 예약인지 확인
        if (!reservation.isPastReservation()) {
            throw IllegalStateException("예약 시간이 지난 후에만 노쇼 처리할 수 있습니다.")
        }
        
        // 노쇼 처리
        reservation.updateStatus(ReservationStatus.NO_SHOW)
    }
    
    /**
     * 점주 예약 취소
     */
    fun cancelReservationByOwner(
        ownerId: Long,
        storeId: Long,
        reservationId: Long,
        cancellationReason: String?
    ) {
        val reservation = getReservationWithValidation(ownerId, storeId, reservationId)
        
        // 취소 가능 상태 체크
        if (!reservation.isCancellable()) {
            throw IllegalStateException("취소할 수 없는 예약 상태입니다.")
        }
        
        // 취소 처리
        reservation.updateStatusWithReason(ReservationStatus.CANCELLED, cancellationReason)
        
        // TODO: FCM 알림 발송 (Phase 2)
        // notificationService.sendReservationCancelledByOwner(reservation)
    }
    
    /**
     * 예약 상세 조회
     */
    @Transactional(readOnly = true)
    fun getReservationDetail(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ): Reservation {
        return getReservationWithValidation(ownerId, storeId, reservationId)
    }
    
    /**
     * ✨ 예약 통계 조회
     */
    @Transactional(readOnly = true)
    fun getReservationStatistics(
        ownerId: Long,
        storeId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): ReservationStatisticsDto {
        // 점포 소유권 확인
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        store.validateOwnership(ownerId)
        
        val startDateTime = startDate.atStartOfDay()
        val endDateTime = endDate.atTime(23, 59, 59)
        
        // 1. 전체 예약 조회
        val reservations = reservationRepository.findByStoreIdAndDateTimeBetween(
            storeId, startDateTime, endDateTime
        )
        
        // 2. 상태별 집계
        val statusCounts = reservations
            .groupBy { it.status }
            .mapValues { it.value.size }
        
        // 3. 총 매출 계산 (완료된 예약만)
        val totalRevenue = reservations
            .filter { it.status == ReservationStatus.COMPLETED }
            .sumOf { it.finalAmount }
        
        // 4. 날짜별 집계
        val byDate = reservations
            .groupBy { it.getReservationDate() }
            .map { (date, reservations) ->
                DailyStatisticsDto(
                    date = date,
                    count = reservations.size,
                    revenue = reservations
                        .filter { it.status == ReservationStatus.COMPLETED }
                        .sumOf { it.finalAmount }
                )
            }
            .sortedBy { it.date }
        
        // 5. 시간대별 집계
        val byTimeSlot = reservations
            .groupBy { it.getReservationTime() }
            .map { (time, reservations) ->
                TimeSlotStatisticsDto(
                    time = time,
                    count = reservations.size,
                    averageAmount = if (reservations.isNotEmpty()) {
                        reservations.sumOf { it.finalAmount } / reservations.size.toBigDecimal()
                    } else {
                        java.math.BigDecimal.ZERO
                    }
                )
            }
            .sortedBy { it.time }
        
        // 6. 인기 메뉴 집계
        val menuCounts = mutableMapOf<String, MenuStatisticsDto>()
        reservations.forEach { reservation ->
            reservation.reservationMenus.forEach { menu ->
                val current = menuCounts.getOrDefault(
                    menu.menuName,
                    MenuStatisticsDto(menu.menuName, 0, java.math.BigDecimal.ZERO)
                )
                menuCounts[menu.menuName] = MenuStatisticsDto(
                    menuName = menu.menuName,
                    count = current.count + 1,
                    revenue = current.revenue + menu.totalPrice
                )
            }
        }
        val topMenus = menuCounts.values.sortedByDescending { it.count }.take(5)
        
        // 7. 통계 계산
        val totalCount = reservations.size
        val completedCount = statusCounts[ReservationStatus.COMPLETED] ?: 0
        val noShowCount = statusCounts[ReservationStatus.NO_SHOW] ?: 0
        
        return ReservationStatisticsDto(
            period = PeriodDto(startDate, endDate),
            summary = SummaryStatisticsDto(
                totalReservations = totalCount,
                completed = completedCount,
                cancelled = statusCounts[ReservationStatus.CANCELLED] ?: 0,
                noShow = noShowCount,
                rejected = statusCounts[ReservationStatus.REJECTED] ?: 0,
                completionRate = if (totalCount > 0) completedCount.toDouble() / totalCount else 0.0,
                noShowRate = if (totalCount > 0) noShowCount.toDouble() / totalCount else 0.0,
                totalRevenue = totalRevenue
            ),
            byStatus = statusCounts,
            byDate = byDate,
            byTimeSlot = byTimeSlot,
            topMenus = topMenus
        )
    }
    
    // ============================================
    // Private Helper Methods
    // ============================================
    
    /**
     * 예약 조회 및 권한 검증
     */
    private fun getReservationWithValidation(
        ownerId: Long,
        storeId: Long,
        reservationId: Long
    ): Reservation {
        // 점포 소유권 확인
        val store = storeRepository.findById(storeId).orElseThrow {
            NotFoundException("점포를 찾을 수 없습니다.")
        }
        store.validateOwnership(ownerId)
        
        // 예약 조회
        val reservation = reservationRepository.findById(reservationId).orElseThrow {
            NotFoundException("예약을 찾을 수 없습니다.")
        }
        
        // 예약이 해당 점포의 것인지 확인
        if (reservation.storeId != storeId) {
            throw ForbiddenException("해당 점포의 예약이 아닙니다.")
        }
        
        return reservation
    }
}
