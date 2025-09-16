package com.carava.carwash.store.service

import com.carava.carwash.common.entity.AddressEntityType
import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.common.repository.AddressRepository
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.domain.store.entity.Store
import com.carava.carwash.store.dto.CreateStoreRequestDto
import com.carava.carwash.store.dto.CreateStoreResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("storeService")
@Transactional(readOnly = true)
class StoreService(
    private val storeRepository: StoreRepository,
    private val addressRepository: AddressRepository,
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

        val address = request.address.toEntity(savedStore.id, AddressEntityType.STORE)
        val savedAddress = addressRepository.save(address)

        return CreateStoreResponseDto(
            storeId = savedStore.id,
            createdAt = savedStore.createdAt
        )
    }

    fun validateStoreOwnership(storeId: Long, ownerMemberId: Long): Store {
        val store = storeRepository.findById(storeId)
            .orElseThrow{ NotFoundException("가게를 찾을 수 없습니다.") }

        store.validateOwnership(ownerMemberId)

        return store
    }

}