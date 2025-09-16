package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.entity.OperatingHour
import org.springframework.data.jpa.repository.JpaRepository

interface OperatingHourRepository : JpaRepository<OperatingHour, Long>{

    fun deleteByStoreId(storeId: Long)
}