package com.example.task77

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery

class PizzeriaRepository(context: Context) {
    private val dao = PizzeriaDatabase.getInstance(context).dao()

    suspend fun ensureSeedData() {
        if (dao.countDishes() == 0) {
            dao.insertDishes(
                listOf(
                    DishEntity(1, "Маргарита", "Пицца", 18.5),
                    DishEntity(2, "Пепперони", "Пицца", 24.0),
                    DishEntity(3, "Карбонара", "Паста", 19.0),
                    DishEntity(4, "Тирамису", "Десерт", 12.0),
                    DishEntity(5, "Кола", "Напиток", 6.5)
                )
            )
        }

        if (dao.countPromotions() == 0) {
            dao.insertPromotions(
                listOf(
                    PromotionEntity(1, "Скидка 10%", 10, true),
                    PromotionEntity(2, "Комбо выходного дня", 15, true),
                    PromotionEntity(3, "Без акции", 0, false)
                )
            )
        }

        if (dao.countOrders() == 0) {
            dao.insertOrders(
                listOf(
                    OrderEntity(1, 1, 1, 2, 33.3, "Минск, Победителей 15-10", "Карта", "Илья", "Курьер", true, "2026-03-10"),
                    OrderEntity(2, 2, 2, 1, 20.4, "Минск, Немига 3-5", "Наличные", "Артём", "Самовывоз", false, "2026-03-11"),
                    OrderEntity(3, 3, null, 3, 57.0, "Минск, Кирова 7-2", "Карта", "Илья", "Курьер", true, "2026-03-11"),
                    OrderEntity(4, 4, 1, 2, 21.6, "Минск, Кальварийская 8-19", "Онлайн", "Марина", "Курьер", true, "2026-03-12"),
                    OrderEntity(5, 5, null, 4, 26.0, "Минск, Победы 1-44", "Наличные", "Артём", "Самовывоз", false, "2026-03-12"),
                    OrderEntity(6, 2, 1, 2, 43.2, "Минск, Пушкина 11-8", "Карта", "Марина", "Курьер", true, "2026-03-13"),
                    OrderEntity(7, 1, null, 1, 18.5, "Минск, Ленина 20-17", "Онлайн", "Илья", "Курьер", true, "2026-03-14")
                )
            )
        }
    }

    suspend fun getAllOrders(): List<OrderDetailRow> {
        return dao.getOrderDetails(SimpleSQLiteQuery(QuerySqlBuilders.orderDetails()))
    }

    suspend fun getAggregate(function: String): AggregateResultRow? {
        return dao.getAggregateResults(SimpleSQLiteQuery(QuerySqlBuilders.aggregate(function))).firstOrNull()
    }

    suspend fun getFilteredOrders(threshold: Double): List<OrderDetailRow> {
        return dao.getOrderDetails(SimpleSQLiteQuery(QuerySqlBuilders.filter(threshold)))
    }

    suspend fun getGrouped(field: String): List<GroupResultRow> {
        return dao.getGroupResults(SimpleSQLiteQuery(QuerySqlBuilders.group(field)))
    }

    suspend fun getSorted(field: String, direction: String): List<OrderDetailRow> {
        return dao.getOrderDetails(SimpleSQLiteQuery(QuerySqlBuilders.sort(field, direction)))
    }
}
