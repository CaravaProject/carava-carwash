package com.carava.carwash.domain.store.Repository

import com.carava.carwash.domain.store.entity.Menu
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository("menuRepository")
interface MenuRepository : JpaRepository<Menu, Long> {
}