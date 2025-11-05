package com.carava.carwash.store.service

import com.carava.carwash.common.exception.NotFoundException
import com.carava.carwash.domain.address.entity.AddressEntityType
import com.carava.carwash.domain.address.repository.AddressRepository
import com.carava.carwash.domain.store.dto.StoreSearchRequest
import com.carava.carwash.domain.store.entity.Store
import com.carava.carwash.domain.store.repository.StoreRepository
import com.carava.carwash.store.dto.*
import org.springframework.data.domain.PageRequest
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
                category = it.menuType
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

    fun searchStores(request: SearchStoreRequestDto): SearchStoreResponseDto {
        val searchRequest = StoreSearchRequest(
            name = request.name,
            region = request.region,
            district = request.district,
            menuType = request.menuType,
            minPrice = request.minPrice,
            maxPrice = request.maxPrice,
            carType = request.carType,
            sortBy = request.sortBy,
            page = request.page
        )

        val pageable = PageRequest.of(request.page, request.size)

        val page = storeRepository.searchStores(searchRequest, pageable)

        println("=== PAGE CONTENT SIZE: ${page.content.size} ===")

        val storeResults = page.content.map { it.toStoreSearchDto() }

        println("=== STORE RESULTS SIZE: ${storeResults.size} ===")

        return SearchStoreResponseDto(
            stores = storeResults,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            currentPage = page.number
        )
    }

    private fun Store.toStoreSearchDto() = StoreSearchDto(
        storeId = this.id,
        name = this.name,
        averageRating = this.averageRating,
        totalReviews = this.totalReviews,
        address = "주소 추가" // TODO
    )
}