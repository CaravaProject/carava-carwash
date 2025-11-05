package com.carava.carwash.store.dto

import com.carava.carwash.domain.store.dto.SortBy
import com.carava.carwash.domain.store.entity.CarType
import com.carava.carwash.domain.store.entity.MenuType

data class SearchStoreRequestDto(
    // 기본 검색
    val name: String? = null,
    val region: String? = null,
    val district: String? = null,
    //TODO: Reservation 구현 후 날짜, 시간 추가

    // 상세 검색
    val menuType: MenuType? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val carType: CarType? = null,

    // 정렬 & 페이징
    val sortBy: SortBy = SortBy.REVIEW_HIGH,
    val page: Int = 0,
    val size: Int = 20
)
