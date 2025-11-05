package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.Store
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository("storeRepository")
interface StoreRepository : JpaRepository<Store, Long>, StoreRepositoryCustom {
    override fun findById(id: Long): Optional<Store>

    @Query("SELECT s FROM store s LEFT JOIN FETCH s.menus WHERE s.id = :storeId")
    fun findStoreWithMenus(storeId: Long): Store?
}