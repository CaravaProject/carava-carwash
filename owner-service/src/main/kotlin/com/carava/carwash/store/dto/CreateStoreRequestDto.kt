package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.entity.StoreCategory

data class CreateStoreRequestDto(
    val name: String,
    val description: String?,
    val phone: String?,
    val category: StoreCategory,
    val address: AddressDto,
)