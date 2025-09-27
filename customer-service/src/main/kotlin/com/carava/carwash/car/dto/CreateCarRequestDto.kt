package com.carava.carwash.car.dto

import com.carava.carwash.car.entity.CarType
import jakarta.validation.constraints.*

data class CreateCarRequestDto(
    @field:NotBlank(message = "브랜드는 필수입니다")
    val brand: String,

    @field:NotBlank(message = "모델명은 필수입니다")
    val model: String,

    @field:Min(value = 1900, message = "연식은 1900년 이후여야 합니다")
    @field:Max(value = 2030, message = "연식은 2030년 이전이어야 합니다")
    val year: Int,

    val color: String? = null,

    @field:NotBlank(message = "차량번호는 필수입니다")
    @field:Pattern(regexp = "^[0-9]{2,3}[가-힣][0-9]{4}$", message = "올바른 차량번호 형식이 아닙니다")
    val licensePlate: String,

    @field:NotNull(message = "차량 타입은 필수입니다")
    val carType: CarType,

    val isDefault: Boolean = false
)