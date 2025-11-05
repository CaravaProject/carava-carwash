package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.entity.CarType
import com.carava.carwash.domain.store.entity.MenuType
import java.math.BigDecimal

data class CreateMenuRequestDto(
    val name: String,
    val carType: CarType,
    val menuType: MenuType,
    val price: BigDecimal,
    val description: String?,
    val duration: Int
)