package com.carava.carwash.store.service

import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.address.entity.AddressEntityType
import com.carava.carwash.domain.address.repository.AddressRepository
import com.carava.carwash.domain.store.entity.Holiday
import com.carava.carwash.domain.store.entity.OperatingHour
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.domain.store.entity.Store
import com.carava.carwash.domain.store.repository.HolidayRepository
import com.carava.carwash.domain.store.repository.OperatingHourRepository
import com.carava.carwash.store.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("storeService")
@Transactional(readOnly = true)
class StoreService(
    private val storeRepository: StoreRepository,
    private val addressRepository: AddressRepository,
    private val holidayRepository: HolidayRepository,
    private val operatingHourRepository: OperatingHourRepository
) {

    @Transactional
    fun createStore(request: CreateStoreRequestDto, ownerMemberId: Long): CreateStoreResponseDto {

        val store = Store(
            name = request.name,
            ownerMemberId = ownerMemberId,
            description = request.description,
            phone = request.phone,
            category = request.category,
        )
        val savedStore = storeRepository.save(store)

        saveStoreAddress(request, savedStore)

        createOperatingHours(savedStore, request.operatingHours)

        return CreateStoreResponseDto(
            storeId = savedStore.id,
            createdAt = savedStore.createdAt
        )
    }

    @Transactional
    fun createHoliday(storeId: Long, request: CreateHolidayRequestDto, memberId: Long) : CreateHolidayResponseDto {

        val store = validateStoreOwnership(storeId, memberId)

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

    @Transactional
    fun saveOperatingHour(storeId: Long, request: SaveOperatingHourRequestDto, memberId: Long) : SaveOperatingHourResponseDto {

        val store = validateStoreOwnership(storeId, memberId)

        operatingHourRepository.deleteByStoreId(storeId)

        val operatingHours = request.operatingHours.map { dto ->
            OperatingHour(
                store = store,
                dayOfWeek = dto.dayOfWeek,
                openTime = dto.openTime,
                closeTime = dto.closeTime,
                isOpen = dto.isOpen,
            )
        }
        val savedOperatingHours = operatingHourRepository.saveAll(operatingHours)

        return SaveOperatingHourResponseDto(
            createdAt = savedOperatingHours.first().createdAt
        )
    }

    fun validateStoreOwnership(storeId: Long, ownerMemberId: Long): Store {
        val store = storeRepository.findById(storeId)
            .orElseThrow{ NotFoundException("가게를 찾을 수 없습니다.") }

        store.validateOwnership(ownerMemberId)

        return store
    }

    private fun saveStoreAddress(request: CreateStoreRequestDto, savedStore: Store) {
        val address = request.address.toEntity(savedStore.id, AddressEntityType.STORE)
        val savedAddress = addressRepository.save(address)
    }

    private fun createOperatingHours(store: Store, operatingHours: List<OperatingHourDto>) {
        val operatingHourEntities = operatingHours.map {
            OperatingHour(
                store = store,
                dayOfWeek = it.dayOfWeek,
                openTime = it.openTime,
                closeTime = it.closeTime,
                isOpen = it.isOpen,
            )
        }
        operatingHourRepository.saveAll(operatingHourEntities)
    }
}