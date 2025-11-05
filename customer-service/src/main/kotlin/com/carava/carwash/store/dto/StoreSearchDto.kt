package com.carava.carwash.store.dto

import java.math.BigDecimal

data class StoreSearchDto(
    val storeId: Long,
    val name: String,
    val averageRating: BigDecimal,
    val totalReviews: Int,
    val address: String,
    //TODO: 사용자와의 거리, 대표 사진 추가
)
