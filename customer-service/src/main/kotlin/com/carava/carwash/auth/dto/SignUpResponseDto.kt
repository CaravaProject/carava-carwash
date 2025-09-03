package com.carava.carwash.auth.dto

import java.time.LocalDateTime

data class SignUpResponseDto (
    val email: String,
    val userType: String,
    val createdAt: LocalDateTime
)