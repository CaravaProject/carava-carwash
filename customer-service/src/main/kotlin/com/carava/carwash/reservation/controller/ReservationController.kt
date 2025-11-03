package com.carava.carwash.reservation.controller

import com.carava.carwash.common.dto.ApiResponse
import com.carava.carwash.infrastructure.annotation.CurrentMemberId
import com.carava.carwash.reservation.dto.*
import com.carava.carwash.reservation.service.CustomerReservationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/reservations")
class ReservationController(
    private val reservationService: CustomerReservationService
) {

    /**
     * 예약 가능 시간 조회
     * GET /api/reservations/available-slots?storeId=1&date=2025-10-21
     */
    @GetMapping("/available-slots")
    fun getAvailableTimeSlots(
        @ModelAttribute request: AvailableTimeSlotsRequest
    ): ApiResponse<AvailableTimeSlotsResponse> {
        
        val slots = reservationService.getAvailableTimeSlots(
            storeId = request.storeId,
            date = request.date
        )
        
        val response = AvailableTimeSlotsResponse(
            date = request.date,
            slots = slots
        )
        
        return ApiResponse.success(response)
    }

    /**
     * 예약 생성
     * POST /api/reservations
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReservation(
        @CurrentMemberId customerId: Long,
        @RequestBody request: CreateReservationRequest
    ): ApiResponse<ReservationResponse> {
        
        val reservation = reservationService.createReservation(
            customerId = customerId,
            storeId = request.storeId,
            carId = request.carId,
            reservationDate = request.reservationDate,
            reservationTime = request.reservationTime,
            menuIds = request.menuIds,
            customerRequest = request.customerRequest
        )
        
        val response = ReservationResponse.from(reservation)
        
        return ApiResponse.success(
            data = response,
            message = "예약이 성공적으로 생성되었습니다."
        )
    }

    /**
     * 내 예약 목록 조회 (활성)
     * GET /api/reservations/my
     */
    @GetMapping("/my")
    fun getMyReservations(
        @CurrentMemberId customerId: Long
    ): ApiResponse<List<ReservationListResponse>> {
        
        val reservations = reservationService.getMyReservations(customerId)
        
        val response = reservations.map { 
            ReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * ✨ 예약 히스토리 조회 (완료/취소 등)
     * GET /api/reservations/history?page=0&size=10
     */
    @GetMapping("/history")
    fun getReservationHistory(
        @CurrentMemberId customerId: Long,
        pageable: org.springframework.data.domain.Pageable
    ): ApiResponse<org.springframework.data.domain.Page<ReservationListResponse>> {
        
        val reservations = reservationService.getReservationHistory(customerId, pageable)
        
        val response = reservations.map { 
            ReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * ✨ 특정 점포에서의 내 예약 히스토리
     * GET /api/reservations/store/{storeId}/history
     */
    @GetMapping("/store/{storeId}/history")
    fun getMyReservationsAtStore(
        @CurrentMemberId customerId: Long,
        @PathVariable storeId: Long
    ): ApiResponse<List<ReservationListResponse>> {
        
        val reservations = reservationService.getMyReservationsAtStore(customerId, storeId)
        
        val response = reservations.map { 
            ReservationListResponse.from(it) 
        }
        
        return ApiResponse.success(response)
    }

    /**
     * 예약 상세 조회
     * GET /api/reservations/{id}
     */
    @GetMapping("/{id}")
    fun getReservationDetail(
        @CurrentMemberId customerId: Long,
        @PathVariable id: Long
    ): ApiResponse<ReservationResponse> {
        
        val reservation = reservationService.getReservationDetail(
            reservationId = id,
            customerId = customerId
        )
        
        val response = ReservationResponse.from(reservation)
        
        return ApiResponse.success(response)
    }

    /**
     * ✨ 예약 수정
     * PUT /api/reservations/{id}
     */
    @PutMapping("/{id}")
    fun updateReservation(
        @CurrentMemberId customerId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateReservationRequest
    ): ApiResponse<ReservationResponse> {
        
        val reservation = reservationService.updateReservation(
            reservationId = id,
            customerId = customerId,
            newReservationDate = request.reservationDate,
            newReservationTime = request.reservationTime,
            newMenuIds = request.menuIds
        )
        
        val response = ReservationResponse.from(reservation)
        
        return ApiResponse.success(
            data = response,
            message = "예약이 수정되었습니다."
        )
    }

    /**
     * ✨ 예약 가능 여부 체크
     * POST /api/reservations/check-availability
     */
    @PostMapping("/check-availability")
    fun checkAvailability(
        @RequestBody request: CheckAvailabilityRequest
    ): ApiResponse<AvailabilityCheckResult> {
        
        val result = reservationService.checkAvailability(
            storeId = request.storeId,
            reservationDate = request.reservationDate,
            reservationTime = request.reservationTime
        )
        
        return ApiResponse.success(result)
    }

    /**
     * 예약 취소
     * DELETE /api/reservations/{id}
     */
    @DeleteMapping("/{id}")
    fun cancelReservation(
        @CurrentMemberId customerId: Long,
        @PathVariable id: Long,
        @RequestBody(required = false) request: CancelReservationRequest?
    ): ApiResponse<Unit> {
        
        reservationService.cancelReservation(
            reservationId = id,
            customerId = customerId,
            cancellationReason = request?.cancellationReason
        )
        
        return ApiResponse.success(
            message = "예약이 취소되었습니다."
        )
    }
}
