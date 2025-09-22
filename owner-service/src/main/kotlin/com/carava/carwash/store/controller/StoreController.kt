package com.carava.carwash.store.controller

import com.carava.carwash.common.dto.ApiResponse
import com.carava.carwash.infrastructure.annotation.CurrentMemberId
import com.carava.carwash.store.dto.*
import com.carava.carwash.store.service.MenuService
import com.carava.carwash.store.service.StoreService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController("storeController")
@RequestMapping("/api/stores")
class StoreController(
    private val storeService: StoreService,
    private val menuService: MenuService,
) {

    @PostMapping
    fun createStore(
        @Valid @RequestBody request: CreateStoreRequestDto,
        @CurrentMemberId memberId: Long,
    ) : ApiResponse<CreateStoreResponseDto> {
        val response = storeService.createStore(request, memberId)
        return ApiResponse.success(data = response)
    }

    @PostMapping("/{storeId}/menus")
    fun createMenu(
        @PathVariable storeId: Long,
        @Valid @RequestBody request: CreateMenuRequestDto,
        @CurrentMemberId memberId: Long,
    ) : ApiResponse<CreateMenuResponseDto> {
        val response = menuService.createMenu(storeId, request, memberId)
        return ApiResponse.success(data = response)
    }


    @PostMapping("/{storeId}/operating-hours")
    fun saveOperatingHours(
        @PathVariable storeId: Long,
        @RequestBody request: SaveOperatingHourRequestDto,
        @CurrentMemberId memberId: Long
    ) : ApiResponse<SaveOperatingHourResponseDto> {
        val response = storeService.saveOperatingHour(storeId, request, memberId)
        return ApiResponse.success(data = response)
    }

    @PostMapping("/{storeId}/holidays")
    fun createHoliday(
        @PathVariable storeId: Long,
        @RequestBody request: CreateHolidayRequestDto,
        @CurrentMemberId memberId: Long
    ) : ApiResponse<CreateHolidayResponseDto> {
        val response = storeService.createHoliday(storeId, request, memberId)
        return ApiResponse.success(data = response)
    }

}