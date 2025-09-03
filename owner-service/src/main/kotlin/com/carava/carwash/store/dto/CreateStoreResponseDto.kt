package com.carava.carwash.store.dto

import java.time.LocalDateTime

data class CreateStoreResponseDto(
    val storeId: Long,
    val createdAt: LocalDateTime,
)