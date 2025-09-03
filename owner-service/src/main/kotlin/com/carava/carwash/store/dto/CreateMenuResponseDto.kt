package com.carava.carwash.store.dto

import java.time.LocalDateTime

data class CreateMenuResponseDto(
    val menuId: Long,
    val name: String,
    val createdAt: LocalDateTime,
)