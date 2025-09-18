package com.carava.carwash.store.service

import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.address.entity.AddressEntityType
import com.carava.carwash.domain.address.repository.AddressRepository
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.store.dto.GetStoreResponseDto
import com.carava.carwash.store.dto.MenuDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("storeService")
@Transactional(readOnly = true)
class StoreService(
    private val storeRepository: StoreRepository,
    private val addressRepository: AddressRepository,
) {

    fun getStore(storeId: Long) : GetStoreResponseDto {
        val store = storeRepository.findStoreWithMenus(storeId)
            ?: throw NotFoundException("가게를 찾을 수 없습니다.")

        val address = addressRepository.findByEntityTypeAndEntityId(AddressEntityType.STORE, storeId)
            ?: throw NotFoundException("가게의 주소를 찾을 수 없습니다.")

        val menus = store.menus.map{
            MenuDto(
                name = it.name,
                price = it.price,
                category = it.category
            )
        }

        return GetStoreResponseDto(
            storeId = storeId,
            name = store.name,
            averageRating = store.averageRating,
            totalReviews = store.totalReviews,
            address = address.fullAddress,
            phone = store.phone,
            description = store.description,
            menus = menus
        )
    }
}