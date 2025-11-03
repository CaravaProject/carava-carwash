package com.carava.carwash.domain.store.entity

import com.carava.carwash.common.exception.ForbiddenException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class StoreTest {

    // ============================================
    // canAcceptMoreReservations 테스트
    // ============================================

    @Test
    fun `예약 수가 용량보다 적으면 true를 반환해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 3)
        
        // When
        val canAccept = store.canAcceptMoreReservations(2) // 2 < 3
        
        // Then
        assertTrue(canAccept)
    }

    @Test
    fun `예약 수가 용량과 같으면 false를 반환해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 3)
        
        // When
        val canAccept = store.canAcceptMoreReservations(3) // 3 < 3 = false
        
        // Then
        assertFalse(canAccept)
    }

    @Test
    fun `예약 수가 용량을 초과하면 false를 반환해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 3)
        
        // When
        val canAccept = store.canAcceptMoreReservations(5) // 5 < 3 = false
        
        // Then
        assertFalse(canAccept)
    }

    // ============================================
    // getAvailableCapacity 테스트
    // ============================================

    @Test
    fun `남은 자리를 정확히 계산해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 5)
        
        // When & Then
        assertEquals(5, store.getAvailableCapacity(0)) // 5 - 0 = 5
        assertEquals(3, store.getAvailableCapacity(2)) // 5 - 2 = 3
        assertEquals(0, store.getAvailableCapacity(5)) // 5 - 5 = 0
    }

    @Test
    fun `예약 수가 용량을 초과해도 음수가 아닌 0을 반환해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 3)
        
        // When
        val available = store.getAvailableCapacity(10) // 3 - 10 = -7, but...
        
        // Then
        assertEquals(0, available) // coerceAtLeast(0) → 0
    }

    @Test
    fun `hourlyCapacity가 2일 때 정확히 작동해야 한다`() {
        // Given
        val store = createStore(hourlyCapacity = 2)
        
        // When & Then
        assertTrue(store.canAcceptMoreReservations(0))   // 0 < 2 ✅
        assertTrue(store.canAcceptMoreReservations(1))   // 1 < 2 ✅
        assertFalse(store.canAcceptMoreReservations(2))  // 2 < 2 ❌
        
        assertEquals(2, store.getAvailableCapacity(0))  // 2 - 0 = 2
        assertEquals(1, store.getAvailableCapacity(1))  // 2 - 1 = 1
        assertEquals(0, store.getAvailableCapacity(2))  // 2 - 2 = 0
    }

    // ============================================
    // validateOwnership 테스트
    // ============================================

    @Test
    fun `소유자가 맞으면 예외가 발생하지 않아야 한다`() {
        // Given
        val store = createStore(ownerMemberId = 100L)
        
        // When & Then
        assertDoesNotThrow {
            store.validateOwnership(100L)
        }
    }

    @Test
    fun `소유자가 아니면 ForbiddenException이 발생해야 한다`() {
        // Given
        val store = createStore(ownerMemberId = 100L)
        
        // When & Then
        val exception = assertThrows<ForbiddenException> {
            store.validateOwnership(999L)
        }
        
        assertEquals("해당 가게에 대한 권한이 없습니다.", exception.message)
    }

    // ============================================
    // Helper Methods
    // ============================================

    private fun createStore(
        id: Long = 1L,
        ownerMemberId: Long = 1L,
        name: String = "테스트세차장",
        hourlyCapacity: Int = 2,
        slotDuration: Int = 30
    ): Store {
        return Store(
            id = id,
            ownerMemberId = ownerMemberId,
            name = name,
            category = StoreCategory.CAR_WASH,
            status = StoreStatus.ACTIVE,
            hourlyCapacity = hourlyCapacity,
            slotDuration = slotDuration
        )
    }
}
