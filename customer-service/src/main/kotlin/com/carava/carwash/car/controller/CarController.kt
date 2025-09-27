package com.carava.carwash.car.controller

import com.carava.carwash.car.dto.CarListResponseDto
import com.carava.carwash.car.dto.CarResponseDto
import com.carava.carwash.car.dto.CreateCarRequestDto
import com.carava.carwash.car.dto.UpdateCarRequestDto
import com.carava.carwash.car.service.CarService
import com.carava.carwash.common.dto.ApiResponse
import com.carava.carwash.infrastructure.annotation.CurrentMemberId
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cars")
class CarController(
    private val carService: CarService
) {
    
    /**
     * 차량 등록
     * POST /api/cars
     */
    @PostMapping
    fun createCar(
        @CurrentMemberId memberId: Long,
        @Valid @RequestBody request: CreateCarRequestDto
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val response = carService.createCar(memberId, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    /**
     * 차량 목록 조회
     * GET /api/cars
     */
    @GetMapping
    fun getCars(
        @CurrentMemberId memberId: Long
    ): ResponseEntity<ApiResponse<CarListResponseDto>> {
        val response = carService.getCarsByMemberId(memberId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 차량 상세 조회
     * GET /api/cars/{carId}
     */
    @GetMapping("/{carId}")
    fun getCar(
        @CurrentMemberId memberId: Long,
        @PathVariable carId: Long
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val response = carService.getCarById(memberId, carId)  // 순서 수정
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 차량 정보 수정
     * PUT /api/cars/{carId}
     */
    @PutMapping("/{carId}")
    fun updateCar(
        @CurrentMemberId memberId: Long,
        @PathVariable carId: Long,
        @Valid @RequestBody request: UpdateCarRequestDto
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val response = carService.updateCar(memberId, carId, request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * 차량 삭제
     * DELETE /api/cars/{carId}
     */
    @DeleteMapping("/{carId}")
    fun deleteCar(
        @CurrentMemberId memberId: Long,
        @PathVariable carId: Long
    ): ResponseEntity<ApiResponse<Void>> {
        carService.deleteCar(memberId, carId)
        return ResponseEntity.noContent().build()
    }

    /**
     * 기본 차량 설정
     * PATCH /api/cars/{carId}/default
     */
    @PatchMapping("/{carId}/default")
    fun setDefaultCar(
        @CurrentMemberId memberId: Long,
        @PathVariable carId: Long
    ): ResponseEntity<ApiResponse<CarResponseDto>> {
        val response = carService.setDefaultCar(memberId, carId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}