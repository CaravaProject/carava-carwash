package com.carava.carwash.member.dto

data class CreateMemberRequestDto(
    val name: String,
    val authId: Long,
)