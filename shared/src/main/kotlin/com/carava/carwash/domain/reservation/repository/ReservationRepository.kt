package com.carava.carwash.domain.reservation.repository

import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface ReservationRepository : JpaRepository<Reservation, Long> {
    
    // ============================================
    // 고객용 쿼리
    // ============================================
    
    // 고객의 예약 목록 조회 (페이징) - 전체
    fun findByCustomerIdOrderByReservationDateTimeDesc(
        customerId: Long,
        pageable: Pageable
    ): Page<Reservation>
    
    // 고객의 특정 상태 예약 조회 (페이징)
    fun findByCustomerIdAndStatusOrderByReservationDateTimeDesc(
        customerId: Long,
        status: ReservationStatus,
        pageable: Pageable
    ): Page<Reservation>
    
    // 고객의 활성 예약 조회 (PENDING, CONFIRMED, IN_PROGRESS)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.customerId = :customerId
        AND r.status IN ('PENDING', 'CONFIRMED', 'IN_PROGRESS')
        ORDER BY r.reservationDateTime ASC
    """)
    fun findActiveReservationsByCustomerId(
        @Param("customerId") customerId: Long
    ): List<Reservation>
    
    // ✨ 추가: 고객의 완료된 예약 조회 (히스토리)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.customerId = :customerId
        AND r.status IN ('COMPLETED', 'CANCELLED', 'NO_SHOW', 'REJECTED')
        ORDER BY r.reservationDateTime DESC
    """)
    fun findHistoryReservationsByCustomerId(
        @Param("customerId") customerId: Long,
        pageable: Pageable
    ): Page<Reservation>
    
    // ✨ 추가: 특정 점포에서의 고객 예약 히스토리
    fun findByCustomerIdAndStoreIdOrderByReservationDateTimeDesc(
        customerId: Long,
        storeId: Long
    ): List<Reservation>
    
    // ============================================
    // 점주용 쿼리
    // ============================================
    
    // 매장의 특정 날짜 예약 조회
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.storeId = :storeId
        AND DATE(r.reservationDateTime) = :date
        ORDER BY r.reservationDateTime ASC
    """)
    fun findByStoreIdAndDate(
        @Param("storeId") storeId: Long,
        @Param("date") date: LocalDate
    ): List<Reservation>
    
    // 매장의 특정 날짜, 특정 상태 예약 조회
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.storeId = :storeId
        AND DATE(r.reservationDateTime) = :date
        AND r.status IN :statuses
        ORDER BY r.reservationDateTime ASC
    """)
    fun findByStoreIdAndDateAndStatusIn(
        @Param("storeId") storeId: Long,
        @Param("date") date: LocalDate,
        @Param("statuses") statuses: List<ReservationStatus>
    ): List<Reservation>
    
    // ✨ 추가: 매장의 특정 기간 예약 조회 (통계용)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.storeId = :storeId
        AND r.reservationDateTime BETWEEN :startDateTime AND :endDateTime
        ORDER BY r.reservationDateTime ASC
    """)
    fun findByStoreIdAndDateTimeBetween(
        @Param("storeId") storeId: Long,
        @Param("startDateTime") startDateTime: LocalDateTime,
        @Param("endDateTime") endDateTime: LocalDateTime
    ): List<Reservation>
    
    // ============================================
    // 예약 가능 여부 체크 (핵심!)
    // ============================================
    
    // 특정 시간대의 예약 수 카운트
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.storeId = :storeId
        AND r.reservationDateTime = :dateTime
        AND r.status IN :statuses
    """)
    fun countByStoreIdAndDateTimeAndStatusIn(
        @Param("storeId") storeId: Long,
        @Param("dateTime") dateTime: LocalDateTime,
        @Param("statuses") statuses: List<ReservationStatus>
    ): Int
    
    // 특정 시간대 예약 존재 여부
    fun existsByStoreIdAndReservationDateTimeAndStatusIn(
        storeId: Long,
        reservationDateTime: LocalDateTime,
        statuses: List<ReservationStatus>
    ): Boolean
    
    // ============================================
    // 알림용 쿼리
    // ============================================
    
    // 다가오는 예약 조회 (리마인더용)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'CONFIRMED'
        AND r.reservationDateTime BETWEEN :startTime AND :endTime
        ORDER BY r.reservationDateTime ASC
    """)
    fun findUpcomingReservations(
        @Param("startTime") startTime: LocalDateTime,
        @Param("endTime") endTime: LocalDateTime
    ): List<Reservation>
    
    // 노쇼 처리 대상 조회 (예약시간 지났는데 CONFIRMED 상태)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'CONFIRMED'
        AND r.reservationDateTime < :cutoffTime
    """)
    fun findPendingNoShowReservations(
        @Param("cutoffTime") cutoffTime: LocalDateTime
    ): List<Reservation>
    
    // ============================================
    // 통계용 쿼리 ✨ 추가
    // ============================================
    
    // 특정 기간 예약 수
    @Query("""
        SELECT COUNT(r) FROM Reservation r
        WHERE r.storeId = :storeId
        AND r.reservationDateTime BETWEEN :startDate AND :endDate
    """)
    fun countByStoreIdAndPeriod(
        @Param("storeId") storeId: Long,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime
    ): Long
    
    // 특정 기간 상태별 예약 수
    @Query("""
        SELECT r.status, COUNT(r)
        FROM Reservation r
        WHERE r.storeId = :storeId
        AND r.reservationDateTime BETWEEN :startDateTime AND :endDateTime
        GROUP BY r.status
    """)
    fun countByStoreIdAndPeriodGroupByStatus(
        @Param("storeId") storeId: Long,
        @Param("startDateTime") startDateTime: LocalDateTime,
        @Param("endDateTime") endDateTime: LocalDateTime
    ): List<Array<Any>>
    
    // 특정 기간 총 매출
    @Query("""
        SELECT COALESCE(SUM(r.finalAmount), 0)
        FROM Reservation r
        WHERE r.storeId = :storeId
        AND r.status = 'COMPLETED'
        AND r.reservationDateTime BETWEEN :startDateTime AND :endDateTime
    """)
    fun sumRevenueByStoreIdAndPeriod(
        @Param("storeId") storeId: Long,
        @Param("startDateTime") startDateTime: LocalDateTime,
        @Param("endDateTime") endDateTime: LocalDateTime
    ): java.math.BigDecimal
}
