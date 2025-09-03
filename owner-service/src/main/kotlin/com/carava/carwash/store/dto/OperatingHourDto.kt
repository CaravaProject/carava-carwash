package com.carava.carwash.store.dto

import java.time.DayOfWeek
import java.time.LocalTime

data class OperatingHourDto(
    val dayOfWeek: DayOfWeek,
    val isOpen: Boolean,
    val openTime: LocalTime?,
    val closeTime: LocalTime?,
)