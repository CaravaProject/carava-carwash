package com.carava.carwash.domain.reservation.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(
    name = "reservations",
    indexes = [
        Index(columnList = "store_id, reservation_date_time"),
        Index(columnList = "customer_id, status"),
        Index(columnList = "store_id, status")
    ]
)
class Reservation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val customerId: Long,

    @Column(nullable = false)
    val storeId: Long,

    @Column(nullable = false)
    val carId: Long,

    @Column(nullable = false)
    val reservationDateTime: LocalDateTime,

    @Column(nullable = false)
    val estimatedDuration: Int = 60,  // 분 단위

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ReservationStatus = ReservationStatus.PENDING,

    @Column(nullable = false)
    val totalAmount: BigDecimal,

    @Column(nullable = false)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val finalAmount: BigDecimal,

    @Column(columnDefinition = "TEXT")
    val customerRequest: String? = null,

    @Column(columnDefinition = "TEXT")
    var rejectionReason: String? = null,

    @Column(columnDefinition = "TEXT")
    var cancellationReason: String? = null,

    @OneToMany(
        mappedBy = "reservation",
        cascade = [CascadeType.PERSIST],
        fetch = FetchType.LAZY
    )
    @OrderBy("id ASC")
    private val _reservationMenus: MutableList<ReservationMenu> = mutableListOf(),

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // 읽기 전용 메뉴 리스트
    val reservationMenus: List<ReservationMenu>
        get() = _reservationMenus.toList()

    // 편의 메서드 - DTO 변환용
    fun getReservationDate(): LocalDate = reservationDateTime.toLocalDate()
    
    fun getReservationTime(): LocalTime = reservationDateTime.toLocalTime()
    
    // 핵심 비즈니스 메서드
    fun addMenu(menu: ReservationMenu) {
        _reservationMenus.add(menu)
        menu.reservation = this
    }
    
    fun updateStatus(newStatus: ReservationStatus) {
        this.status = newStatus
        this.updatedAt = LocalDateTime.now()
    }
    
    fun updateStatusWithReason(newStatus: ReservationStatus, reason: String?) {
        this.status = newStatus
        when (newStatus) {
            ReservationStatus.REJECTED -> this.rejectionReason = reason
            ReservationStatus.CANCELLED -> this.cancellationReason = reason
            else -> {}
        }
        this.updatedAt = LocalDateTime.now()
    }
    
    // 종료시간 계산
    fun getEndDateTime(): LocalDateTime {
        return reservationDateTime.plusMinutes(estimatedDuration.toLong())
    }
    
    // 예약 시간이 지났는지 확인
    fun isPastReservation(now: LocalDateTime = LocalDateTime.now()): Boolean {
        return now.isAfter(reservationDateTime)
    }
    
    // 취소 가능 여부
    fun isCancellable(): Boolean {
        return status.isActive()
    }
}
