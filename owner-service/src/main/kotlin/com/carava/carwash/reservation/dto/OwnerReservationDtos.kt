package com.carava.carwash.reservation.dto

import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// ============================================
// Request DTOs
// ============================================

/**
 * 예약 목록 조회 요청
 */
data class GetReservationsRequest(
    val storeId: Long,
    val date: LocalDate,
    val statuses: List<ReservationStatus>? = null
)

/**
 * 예약 거절 요청
 */
data class RejectReservationRequest(
    val rejectionReason: String?
)

/**
 * 예약 취소 요청 (점주)
 */
data class CancelReservationByOwnerRequest(
    val cancellationReason: String?
)

// ============================================
// Response DTOs
// ============================================

/**
 * 점주용 예약 응답 (고객 정보 포함)
 */
data class OwnerReservationResponse(
    val id: Long,
    val customerId: Long,
    val customerName: String?,  // TODO: Member 조회 후 추가
    val customerPhone: String?, // TODO: Member 조회 후 추가
    val carId: Long,
    val carLicensePlate: String?, // TODO: Car 조회 후 추가
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val estimatedDuration: Int,
    val status: ReservationStatus,
    val totalAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val customerRequest: String?,
    val rejectionReason: String?,
    val cancellationReason: String?,
    val menus: List<OwnerReservationMenuResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            reservation: Reservation,
            customerName: String? = null,
            customerPhone: String? = null,
            carLicensePlate: String? = null
        ): OwnerReservationResponse {
            return OwnerReservationResponse(
                id = reservation.id,
                customerId = reservation.customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                carId = reservation.carId,
                carLicensePlate = carLicensePlate,
                reservationDate = reservation.getReservationDate(),
                reservationTime = reservation.getReservationTime(),
                estimatedDuration = reservation.estimatedDuration,
                status = reservation.status,
                totalAmount = reservation.totalAmount,
                finalAmount = reservation.finalAmount,
                customerRequest = reservation.customerRequest,
                rejectionReason = reservation.rejectionReason,
                cancellationReason = reservation.cancellationReason,
                menus = reservation.reservationMenus.map { 
                    OwnerReservationMenuResponse.from(it) 
                },
                createdAt = reservation.createdAt,
                updatedAt = reservation.updatedAt
            )
        }
    }
}

/**
 * 점주용 예약 메뉴 응답
 */
data class OwnerReservationMenuResponse(
    val id: Long,
    val menuName: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val duration: Int
) {
    companion object {
        fun from(reservationMenu: com.carava.carwash.domain.reservation.entity.ReservationMenu): OwnerReservationMenuResponse {
            return OwnerReservationMenuResponse(
                id = reservationMenu.id,
                menuName = reservationMenu.menuName,
                unitPrice = reservationMenu.unitPrice,
                quantity = reservationMenu.quantity,
                totalPrice = reservationMenu.totalPrice,
                duration = reservationMenu.duration
            )
        }
    }
}

/**
 * 점주용 예약 목록 응답 (간단)
 */
data class OwnerReservationListResponse(
    val id: Long,
    val customerId: Long,
    val customerName: String?,
    val carLicensePlate: String?,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val status: ReservationStatus,
    val finalAmount: BigDecimal,
    val menuSummary: String
) {
    companion object {
        fun from(
            reservation: Reservation,
            customerName: String? = null,
            carLicensePlate: String? = null
        ): OwnerReservationListResponse {
            val menuNames = reservation.reservationMenus.map { it.menuName }
            val menuSummary = when {
                menuNames.isEmpty() -> ""
                menuNames.size == 1 -> menuNames[0]
                menuNames.size == 2 -> "${menuNames[0]}, ${menuNames[1]}"
                else -> "${menuNames[0]}, ${menuNames[1]} 외 ${menuNames.size - 2}건"
            }
            
            return OwnerReservationListResponse(
                id = reservation.id,
                customerId = reservation.customerId,
                customerName = customerName,
                carLicensePlate = carLicensePlate,
                reservationDate = reservation.getReservationDate(),
                reservationTime = reservation.getReservationTime(),
                status = reservation.status,
                finalAmount = reservation.finalAmount,
                menuSummary = menuSummary
            )
        }
    }
}

/**
 * 예약 통계 응답
 */
data class ReservationStatisticsResponse(
    val date: LocalDate,
    val totalCount: Int,
    val pendingCount: Int,
    val confirmedCount: Int,
    val inProgressCount: Int,
    val completedCount: Int,
    val cancelledCount: Int,
    val noShowCount: Int,
    val rejectedCount: Int,
    val totalRevenue: BigDecimal
)

/**
 * ✨ 예약 통계 상세 응답
 */
data class ReservationStatisticsDto(
    val period: PeriodDto,
    val summary: SummaryStatisticsDto,
    val byStatus: Map<ReservationStatus, Int>,
    val byDate: List<DailyStatisticsDto>,
    val byTimeSlot: List<TimeSlotStatisticsDto>,
    val topMenus: List<MenuStatisticsDto>
)

data class PeriodDto(
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class SummaryStatisticsDto(
    val totalReservations: Int,
    val completed: Int,
    val cancelled: Int,
    val noShow: Int,
    val rejected: Int,
    val completionRate: Double,
    val noShowRate: Double,
    val totalRevenue: BigDecimal
)

data class DailyStatisticsDto(
    val date: LocalDate,
    val count: Int,
    val revenue: BigDecimal
)

data class TimeSlotStatisticsDto(
    val time: LocalTime,
    val count: Int,
    val averageAmount: BigDecimal
)

data class MenuStatisticsDto(
    val menuName: String,
    val count: Int,
    val revenue: BigDecimal
)
