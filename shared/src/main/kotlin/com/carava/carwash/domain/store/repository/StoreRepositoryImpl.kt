package com.carava.carwash.domain.store.repository

import com.carava.carwash.domain.store.dto.SortBy
import com.carava.carwash.domain.store.dto.StoreSearchRequest
import com.carava.carwash.domain.store.entity.*
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
class StoreRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : StoreRepositoryCustom {
    override fun searchStores(request: StoreSearchRequest, pageable: Pageable): Page<Store> {
        val store = QStore.store
        val menu = QMenu.menu

        println("=== 1 ===")

        val totalCount = queryFactory
            .select(store.countDistinct())
            .from(store)
            .leftJoin(store.menus, menu)
            .where(
                nameContains(request.name),
                hasMenuType(request.menuType),
                hasCarType(request.carType),
                hasPriceInRange(request.minPrice, request.maxPrice)
            )
            .fetchOne() ?: 0L

        println("=========")
        println(totalCount)
        println("=========")

        val results = queryFactory
            .selectFrom(store)
            .distinct()
            .leftJoin(store.menus, menu).fetchJoin()
            .where(
                nameContains(request.name),
                hasMenuType(request.menuType),
                hasCarType(request.carType),
                hasPriceInRange(request.minPrice, request.maxPrice)
            )
            .orderBy(getOrderSpecifier(request.sortBy, store))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        println("=== RESULTS SIZE: ${results.size} ===")
        println("=== RESULTS: $results ===")

        return PageImpl(results, pageable, totalCount)
    }

    private fun nameContains(name: String?): BooleanExpression? =
        name?.let{ QStore.store.name.contains(it) }

    private fun hasMenuType(menuType: MenuType?): BooleanExpression? =
        menuType?.let{ QMenu.menu.menuType.eq(it) }

    private fun hasCarType(carType: CarType?): BooleanExpression? =
        carType?.let{ QMenu.menu.carType.eq(it) }

    private fun hasPriceInRange(minPrice: Int?, maxPrice: Int?): BooleanExpression? {
        if(minPrice != null && maxPrice != null) {
            return QMenu.menu.price.between(BigDecimal(minPrice), BigDecimal(maxPrice))
        }
        if(minPrice != null) {
            return QMenu.menu.price.goe(BigDecimal(minPrice))
        }
        if(maxPrice != null) {
            return QMenu.menu.price.loe(BigDecimal(maxPrice))
        }
        return null
    }

    private fun getOrderSpecifier(sortBy: SortBy, store: QStore) : OrderSpecifier<*> {
        return when(sortBy) {
            SortBy.PRICE_LOW -> store.id.asc() // TODO: 검색한 메뉴의 가격 순?
            SortBy.PRICE_HIGH -> store.id.asc()
            SortBy.LIKE_HIGH -> store.favoriteCount.desc()
            SortBy.REVIEW_HIGH -> store.totalReviews.desc()
            SortBy.RATING_HIGH -> store.averageRating.desc()
            SortBy.DISTANCE_NEAR -> store.id.asc() // TODO: 거리 순 구현
        }
    }
}