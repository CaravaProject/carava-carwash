package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.entity.MenuCategory
import java.math.BigDecimal

data class MenuDto(
    val name: String,
    val price: BigDecimal,
    val category: MenuCategory,
)
