package com.carava.carwash.store.dto

data class SearchStoreResponseDto(
    val stores: List<StoreSearchDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)
