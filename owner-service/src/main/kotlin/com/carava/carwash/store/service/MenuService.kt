package com.carava.carwash.store.service

import com.carava.carwash.domain.store.repository.MenuRepository
import com.carava.carwash.domain.store.entity.Menu
import com.carava.carwash.store.dto.CreateMenuRequestDto
import com.carava.carwash.store.dto.CreateMenuResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service("menuService")
@Transactional(readOnly = true)
class MenuService(
    private val menuRepository: MenuRepository,
    private val storeService: StoreService,
) {

    @Transactional
    fun createMenu(storeId: Long, request: CreateMenuRequestDto, memberId: Long) : CreateMenuResponseDto {

        val store = storeService.validateStoreOwnership(storeId, memberId)

        val nextDisplayOrder = (menuRepository.findMaxDisplayOrderByStoreId(storeId) ?: 0) + 1

        val menu = Menu(
            store = store,
            name = request.name,
            carType = request.carType,
            menuType = request.menuType,
            price = request.price,
            description = request.description,
            duration = request.duration,
            displayOrder = nextDisplayOrder
        )
        val savedMenu = menuRepository.save(menu)

        return CreateMenuResponseDto(
            menuId = savedMenu.id,
            name = savedMenu.name,
            createdAt = savedMenu.createdAt
        )

    }

}