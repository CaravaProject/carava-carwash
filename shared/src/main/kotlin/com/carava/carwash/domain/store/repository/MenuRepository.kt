package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.Menu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository("menuRepository")
interface MenuRepository : JpaRepository<Menu, Long> {

    @Query("SELECT COALESCE(MAX(m.displayOrder), 0) FROM menu m WHERE m.store.id = :storeId")
    fun findMaxDisplayOrderByStoreId(storeId: Long): Int?
}