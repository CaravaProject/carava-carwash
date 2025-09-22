package com.carava.carwash.car.dto

data class CarListResponseDto(
    val cars: List<CarResponseDto>,
    val totalCount: Int
)
