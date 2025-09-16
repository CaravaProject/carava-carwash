package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.entity.MenuCategory
import java.math.BigDecimal

data class CreateMenuRequestDto(
    val name: String,
    val category: MenuCategory?,
    val price: BigDecimal,
    val description: String?,
    val duration: Int
)