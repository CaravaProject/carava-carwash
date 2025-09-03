package com.carava.carwash.store.dto

import java.math.BigDecimal

data class CreateMenuRequestDto(
    val name: String,
    val price: BigDecimal,
)