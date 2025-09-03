package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.entity.StoreCategory

data class CreateStoreRequestDto(
    val name: String,
    val category: StoreCategory
)