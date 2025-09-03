package com.carava.carwash.domain.store.Repository

import com.carava.carwash.domain.store.entity.Store
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository("storeRepository")
interface StoreRepository : JpaRepository<Store, Long> {
    override fun findById(id: Long): Optional<Store>
}