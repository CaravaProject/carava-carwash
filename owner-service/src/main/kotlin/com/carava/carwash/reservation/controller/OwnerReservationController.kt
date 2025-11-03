package com.carava.carwash.reservation.controller

import com.carava.carwash.common.dto.ApiResponse
import com.carava.carwash.domain.reservation.entity.ReservationStatus
import com.carava.carwash.infrastructure.annotation.CurrentMemberId
import com.carava.carwash.reservation.dto.*
import com.carava.carwash.reservation.service.OwnerReservationService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/owner/reservations")
class OwnerReservationController(
    private val ownerReservationService: OwnerReservationService
) {

    /**
     * 오늘의 예약 목록
     * GET /api/owner/reservations/today?storeId=1
     */
    @GetMapping("/today")
    fun getTodayReservations(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long
    ): ApiResponse<List<OwnerReservationListResponse>> {
        
        val reservations = ownerReservationService.getTodayReservations(ownerId, storeId)
        
        val response = reservations.map { 
            OwnerReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * 날짜별 예약 목록
     * GET /api/owner/reservations?storeId=1&date=2025-10-21
     */
    @GetMapping
    fun getReservationsByDate(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ApiResponse<List<OwnerReservationListResponse>> {
        
        val reservations = ownerReservationService.getReservationsByDate(ownerId, storeId, date)
        
        val response = reservations.map { 
            OwnerReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * 대기 중인 예약 목록
     * GET /api/owner/reservations/pending?storeId=1&date=2025-10-21
     */
    @GetMapping("/pending")
    fun getPendingReservations(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ApiResponse<List<OwnerReservationListResponse>> {
        
        val reservations = ownerReservationService.getPendingReservations(ownerId, storeId, date)
        
        val response = reservations.map { 
            OwnerReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }
    
    /**
     * 상태별 예약 목록
     * GET /api/owner/reservations/by-status?storeId=1&date=2025-10-21&statuses=CONFIRMED,IN_PROGRESS
     */
    @GetMapping("/by-status")
    fun getReservationsByStatus(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
        @RequestParam statuses: List<ReservationStatus>
    ): ApiResponse<List<OwnerReservationListResponse>> {
        
        val reservations = ownerReservationService.getReservationsByDateAndStatus(
            ownerId, storeId, date, statuses
        )
        
        val response = reservations.map { 
            OwnerReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * 예약 상세 조회
     * GET /api/owner/reservations/{reservationId}?storeId=1
     */
    @GetMapping("/{reservationId}")
    fun getReservationDetail(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long
    ): ApiResponse<OwnerReservationResponse> {
        
        val reservation = ownerReservationService.getReservationDetail(
            ownerId, storeId, reservationId
        )
        
        val response = OwnerReservationResponse.from(reservation)
        
        return ApiResponse.success(response)
    }

    /**
     * 예약 승인
     * POST /api/owner/reservations/{reservationId}/confirm?storeId=1
     */
    @PostMapping("/{reservationId}/confirm")
    fun confirmReservation(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long
    ): ApiResponse<Unit> {
        
        ownerReservationService.confirmReservation(ownerId, storeId, reservationId)
        
        return ApiResponse.success(
            message = "예약이 승인되었습니다."
        )
    }

    /**
     * 예약 거절
     * POST /api/owner/reservations/{reservationId}/reject?storeId=1
     */
    @PostMapping("/{reservationId}/reject")
    fun rejectReservation(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long,
        @RequestBody(required = false) request: RejectReservationRequest?
    ): ApiResponse<Unit> {
        
        ownerReservationService.rejectReservation(
            ownerId, 
            storeId, 
            reservationId,
            request?.rejectionReason
        )
        
        return ApiResponse.success(
            message = "예약이 거절되었습니다."
        )
    }

    /**
     * 예약 시작 (진행중으로 변경)
     * POST /api/owner/reservations/{reservationId}/start?storeId=1
     */
    @PostMapping("/{reservationId}/start")
    fun startReservation(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long
    ): ApiResponse<Unit> {
        
        ownerReservationService.startReservation(ownerId, storeId, reservationId)
        
        return ApiResponse.success(
            message = "예약이 시작되었습니다."
        )
    }

    /**
     * 예약 완료
     * POST /api/owner/reservations/{reservationId}/complete?storeId=1
     */
    @PostMapping("/{reservationId}/complete")
    fun completeReservation(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long
    ): ApiResponse<Unit> {
        
        ownerReservationService.completeReservation(ownerId, storeId, reservationId)
        
        return ApiResponse.success(
            message = "예약이 완료되었습니다."
        )
    }

    /**
     * 노쇼 처리
     * POST /api/owner/reservations/{reservationId}/no-show?storeId=1
     */
    @PostMapping("/{reservationId}/no-show")
    fun markAsNoShow(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long
    ): ApiResponse<Unit> {
        
        ownerReservationService.markAsNoShow(ownerId, storeId, reservationId)
        
        return ApiResponse.success(
            message = "노쇼 처리되었습니다."
        )
    }

    /**
     * 예약 취소 (점주)
     * POST /api/owner/reservations/{reservationId}/cancel?storeId=1
     */
    @PostMapping("/{reservationId}/cancel")
    fun cancelReservation(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @PathVariable reservationId: Long,
        @RequestBody(required = false) request: CancelReservationByOwnerRequest?
    ): ApiResponse<Unit> {
        
        ownerReservationService.cancelReservationByOwner(
            ownerId,
            storeId,
            reservationId,
            request?.cancellationReason
        )
        
        return ApiResponse.success(
            message = "예약이 취소되었습니다."
        )
    }

    /**
     * ✨ 예약 통계 조회
     * GET /api/owner/reservations/statistics?storeId=1&startDate=2025-10-01&endDate=2025-10-31
     */
    @GetMapping("/statistics")
    fun getReservationStatistics(
        @CurrentMemberId ownerId: Long,
        @RequestParam storeId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ApiResponse<ReservationStatisticsDto> {
        
        val statistics = ownerReservationService.getReservationStatistics(
            ownerId, storeId, startDate, endDate
        )
        
        return ApiResponse.success(statistics)
    }
}
