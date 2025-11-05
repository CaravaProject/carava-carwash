package com.carava.carwash.domain.store.dto

import com.carava.carwash.domain.store.entity.CarType
import com.carava.carwash.domain.store.entity.MenuType

data class StoreSearchRequest(
    val name: String? = null,
    val region: String? = null,
    val district: String? = null,

    val menuType: MenuType? = null,
    val carType: CarType? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,

    val sortBy: SortBy = SortBy.PRICE_LOW,

    val page: Int = 0
)