package com.carava.carwash.store.service

import com.carava.carwash.domain.store.repository.HolidayRepository
import com.carava.carwash.domain.store.entity.Holiday
import com.carava.carwash.store.dto.CreateHolidayRequestDto
import com.carava.carwash.store.dto.CreateHolidayResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("holidayService")
@Transactional(readOnly = true)
class HolidayService (
    val holidayRepository: HolidayRepository,
    val storeService: StoreService,
) {

    @Transactional
    fun createHoliday(storeId: Long, request: CreateHolidayRequestDto, memberId: Long) : CreateHolidayResponseDto {

        val store = storeService.validateStoreOwnership(storeId, memberId)

        val holidays = request.holidays.map { dto ->
            Holiday(
                store = store,
                date = dto.date,
                reason = request.reason
            )
        }

        val savedHolidays = holidayRepository.saveAll(holidays)

        return CreateHolidayResponseDto(
            createdAt = savedHolidays.first().createdAt
        )
    }

}