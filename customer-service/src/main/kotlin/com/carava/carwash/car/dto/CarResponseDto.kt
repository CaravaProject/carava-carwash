package com.carava.carwash.car.dto

import com.carava.carwash.car.entity.CarType
import java.time.LocalDateTime

data class CarResponseDto(
    val id: Long,
    val brand: String,
    val model: String,
    val year: Int,
    val color: String?,
    val licensePlate: String,
    val carType: CarType,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)