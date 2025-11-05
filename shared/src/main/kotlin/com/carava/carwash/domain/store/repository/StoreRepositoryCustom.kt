package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.dto.StoreSearchRequest
import com.carava.carwash.domain.store.entity.Store
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface StoreRepositoryCustom {

    fun searchStores(request: StoreSearchRequest, pageable: Pageable) : Page<Store>
}