package com.carava.carwash.store.controller

import com.carava.carwash.common.dto.ApiResponse
import com.carava.carwash.store.dto.GetStoreResponseDto
import com.carava.carwash.store.dto.SearchStoreRequestDto
import com.carava.carwash.store.dto.SearchStoreResponseDto
import com.carava.carwash.store.service.StoreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController("storeController")
@RequestMapping("/api/stores")
class StoreController(
    private val storeService: StoreService,
) {

    @GetMapping("/{storeId}")
    fun getStore(
        @PathVariable storeId: Long
    ): ApiResponse<GetStoreResponseDto> {
        val response = storeService.getStore(storeId)
        return ApiResponse.success(data = response)
    }

    @GetMapping("/research")
    fun searchStores(
        @ModelAttribute request: SearchStoreRequestDto
    ): ApiResponse<SearchStoreResponseDto> {
        val response = storeService.searchStores(request)
        return ApiResponse.success(data = response)
    }
}
