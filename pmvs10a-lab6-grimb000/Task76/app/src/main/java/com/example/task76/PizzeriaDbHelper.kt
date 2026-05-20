package com.example.task76

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PizzeriaDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${PizzeriaContract.TABLE_DISHES} (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                price REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${PizzeriaContract.TABLE_PROMOTIONS} (
                id INTEGER PRIMARY KEY,
                title TEXT NOT NULL,
                discount_percent INTEGER NOT NULL,
                is_active INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ${PizzeriaContract.TABLE_ORDERS} (
                id INTEGER PRIMARY KEY,
                dish_id INTEGER NOT NULL,
                promotion_id INTEGER,
                quantity INTEGER NOT NULL,
                total_cost REAL NOT NULL,
                delivery_address TEXT NOT NULL,
                payment_method TEXT NOT NULL,
                courier_name TEXT NOT NULL,
                delivery_method TEXT NOT NULL,
                is_delivery INTEGER NOT NULL,
                order_date TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun ensureSeedData() {
        val db = writableDatabase
        if (isTableEmpty(db, PizzeriaContract.TABLE_DISHES)) {
            seedDishes(db)
        }
        if (isTableEmpty(db, PizzeriaContract.TABLE_PROMOTIONS)) {
            seedPromotions(db)
        }
        if (isTableEmpty(db, PizzeriaContract.TABLE_ORDERS)) {
            seedOrders(db)
        }
    }

    private fun isTableEmpty(db: SQLiteDatabase, tableName: String): Boolean {
        db.rawQuery("SELECT COUNT(*) FROM $tableName", null).use { cursor ->
            return !cursor.moveToFirst() || cursor.getInt(0) == 0
        }
    }

    private fun seedDishes(db: SQLiteDatabase) {
        listOf(
            arrayOf(1, "Маргарита", "Пицца", 18.5),
            arrayOf(2, "Пепперони", "Пицца", 24.0),
            arrayOf(3, "Карбонара", "Паста", 19.0),
            arrayOf(4, "Тирамису", "Десерт", 12.0),
            arrayOf(5, "Кола", "Напиток", 6.5)
        ).forEach { dish ->
            db.insert(PizzeriaContract.TABLE_DISHES, null, ContentValues().apply {
                put("id", dish[0] as Int)
                put("name", dish[1] as String)
                put("category", dish[2] as String)
                put("price", dish[3] as Double)
            })
        }
    }

    private fun seedPromotions(db: SQLiteDatabase) {
        listOf(
            arrayOf(1, "Скидка 10%", 10, 1),
            arrayOf(2, "Комбо выходного дня", 15, 1),
            arrayOf(3, "Без акции", 0, 0)
        ).forEach { promotion ->
            db.insert(PizzeriaContract.TABLE_PROMOTIONS, null, ContentValues().apply {
                put("id", promotion[0] as Int)
                put("title", promotion[1] as String)
                put("discount_percent", promotion[2] as Int)
                put("is_active", promotion[3] as Int)
            })
        }
    }

    private fun seedOrders(db: SQLiteDatabase) {
        listOf(
            orderValues(1, 1, 1, 2, 33.3, "Минск, Победителей 15-10", "Карта", "Илья", "Курьер", 1, "2026-03-10"),
            orderValues(2, 2, 2, 1, 20.4, "Минск, Немига 3-5", "Наличные", "Артём", "Самовывоз", 0, "2026-03-11"),
            orderValues(3, 3, null, 3, 57.0, "Минск, Кирова 7-2", "Карта", "Илья", "Курьер", 1, "2026-03-11"),
            orderValues(4, 4, 1, 2, 21.6, "Минск, Кальварийская 8-19", "Онлайн", "Марина", "Курьер", 1, "2026-03-12"),
            orderValues(5, 5, null, 4, 26.0, "Минск, Победы 1-44", "Наличные", "Артём", "Самовывоз", 0, "2026-03-12"),
            orderValues(6, 2, 1, 2, 43.2, "Минск, Пушкина 11-8", "Карта", "Марина", "Курьер", 1, "2026-03-13"),
            orderValues(7, 1, null, 1, 18.5, "Минск, Ленина 20-17", "Онлайн", "Илья", "Курьер", 1, "2026-03-14")
        ).forEach { db.insert(PizzeriaContract.TABLE_ORDERS, null, it) }
    }

    private fun orderValues(
        id: Int,
        dishId: Int,
        promotionId: Int?,
        quantity: Int,
        totalCost: Double,
        address: String,
        paymentMethod: String,
        courierName: String,
        deliveryMethod: String,
        isDelivery: Int,
        orderDate: String
    ): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put("dish_id", dishId)
            if (promotionId == null) putNull("promotion_id") else put("promotion_id", promotionId)
            put("quantity", quantity)
            put("total_cost", totalCost)
            put("delivery_address", address)
            put("payment_method", paymentMethod)
            put("courier_name", courierName)
            put("delivery_method", deliveryMethod)
            put("is_delivery", isDelivery)
            put("order_date", orderDate)
        }
    }

    companion object {
        private const val DATABASE_NAME = "pizzeria.db"
        private const val DATABASE_VERSION = 1
    }
}
