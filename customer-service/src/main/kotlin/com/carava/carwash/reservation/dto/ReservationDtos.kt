package com.carava.carwash.reservation.dto

import com.carava.carwash.domain.reservation.entity.Reservation
import com.carava.carwash.domain.reservation.entity.ReservationMenu
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

// ============================================
// Request DTOs
// ============================================

/**
 * 예약 생성 요청
 */
data class CreateReservationRequest(
    val storeId: Long,
    val carId: Long,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val menuIds: List<Long>,
    val customerRequest: String? = null
) {
    fun toReservationDateTime(): LocalDateTime {
        return LocalDateTime.of(reservationDate, reservationTime)
    }
}

/**
 * 예약 취소 요청
 */
data class CancelReservationRequest(
    val cancellationReason: String? = null
)

/**
 * ✨ 예약 수정 요청
 */
data class UpdateReservationRequest(
    val reservationDate: LocalDate?,
    val reservationTime: LocalTime?,
    val menuIds: List<Long>?
)

/**
 * ✨ 예약 가능 여부 체크 요청
 */
data class CheckAvailabilityRequest(
    val storeId: Long,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime
)

/**
 * 예약 가능 시간 조회 요청
 */
data class AvailableTimeSlotsRequest(
    val storeId: Long,
    val date: LocalDate
)

// ============================================
// Response DTOs
// ============================================

/**
 * 예약 응답 (상세)
 */
data class ReservationResponse(
    val id: Long,
    val customerId: Long,
    val storeId: Long,
    val storeName: String?,
    val carId: Long,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val estimatedDuration: Int,
    val status: ReservationStatus,
    val totalAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val finalAmount: BigDecimal,
    val customerRequest: String?,
    val rejectionReason: String?,
    val cancellationReason: String?,
    val menus: List<ReservationMenuResponse>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(
            reservation: Reservation,
            storeName: String? = null
        ): ReservationResponse {
            return ReservationResponse(
                id = reservation.id,
                customerId = reservation.customerId,
                storeId = reservation.storeId,
                storeName = storeName,
                carId = reservation.carId,
                reservationDate = reservation.getReservationDate(),
                reservationTime = reservation.getReservationTime(),
                estimatedDuration = reservation.estimatedDuration,
                status = reservation.status,
                totalAmount = reservation.totalAmount,
                discountAmount = reservation.discountAmount,
                finalAmount = reservation.finalAmount,
                customerRequest = reservation.customerRequest,
                rejectionReason = reservation.rejectionReason,
                cancellationReason = reservation.cancellationReason,
                menus = reservation.reservationMenus.map { 
                    ReservationMenuResponse.from(it) 
                },
                createdAt = reservation.createdAt,
                updatedAt = reservation.updatedAt
            )
        }
    }
}

/**
 * 예약 메뉴 응답
 */
data class ReservationMenuResponse(
    val id: Long,
    val menuId: Long,
    val menuName: String,
    val menuDescription: String?,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val totalPrice: BigDecimal,
    val duration: Int,
    val carType: String,
    val menuType: String,
) {
    companion object {
        fun from(reservationMenu: ReservationMenu): ReservationMenuResponse {
            return ReservationMenuResponse(
                id = reservationMenu.id,
                menuId = reservationMenu.menuId,
                menuName = reservationMenu.menuName,
                menuDescription = reservationMenu.menuDescription,
                unitPrice = reservationMenu.unitPrice,
                quantity = reservationMenu.quantity,
                totalPrice = reservationMenu.totalPrice,
                duration = reservationMenu.duration,
                carType = reservationMenu.carType,
                menuType = reservationMenu.menuType,
            )
        }
    }
}

/**
 * 예약 목록 응답 (간단)
 */
data class ReservationListResponse(
    val id: Long,
    val storeId: Long,
    val storeName: String?,
    val reservationDate: LocalDate,
    val reservationTime: LocalTime,
    val status: ReservationStatus,
    val finalAmount: BigDecimal,
    val menuSummary: String
) {
    companion object {
        fun from(
            reservation: Reservation,
            storeName: String? = null
        ): ReservationListResponse {
            val menuNames = reservation.reservationMenus.map { it.menuName }
            val menuSummary = when {
                menuNames.isEmpty() -> ""
                menuNames.size == 1 -> menuNames[0]
                menuNames.size == 2 -> "${menuNames[0]}, ${menuNames[1]}"
                else -> "${menuNames[0]}, ${menuNames[1]} 외 ${menuNames.size - 2}건"
            }
            
            return ReservationListResponse(
                id = reservation.id,
                storeId = reservation.storeId,
                storeName = storeName,
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
 * 예약 가능 시간 슬롯 응답
 */
data class AvailableTimeSlotsResponse(
    val date: LocalDate,
    val slots: List<TimeSlotDto>
)

data class TimeSlotDto(
    val time: LocalTime,
    val availableCount: Int,
    val totalCapacity: Int,
    val isAvailable: Boolean
)

/**
 * ✨ 예약 가능 여부 체크 응답
 */
data class AvailabilityCheckResult(
    val available: Boolean,
    val availableCount: Int,
    val totalCapacity: Int,
    val message: String
)
