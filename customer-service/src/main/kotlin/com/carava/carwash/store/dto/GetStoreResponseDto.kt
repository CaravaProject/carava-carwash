package com.carava.carwash.store.dto

import java.math.BigDecimal

data class GetStoreResponseDto(
    val storeId: Long,
    val name: String,
    val averageRating: BigDecimal,
    val totalReviews: Int,
    val address: String,
    val phone: String,
    val description: String?,
    val menus: List<MenuDto>,
)
