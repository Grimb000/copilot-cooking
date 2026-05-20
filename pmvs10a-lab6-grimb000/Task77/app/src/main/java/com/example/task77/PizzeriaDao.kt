package com.example.task77

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
interface PizzeriaDao {
    @Query("SELECT COUNT(*) FROM dishes")
    suspend fun countDishes(): Int

    @Query("SELECT COUNT(*) FROM promotions")
    suspend fun countPromotions(): Int

    @Query("SELECT COUNT(*) FROM orders")
    suspend fun countOrders(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDishes(items: List<DishEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(items: List<PromotionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(items: List<OrderEntity>)

    @RawQuery
    suspend fun getOrderDetails(query: SupportSQLiteQuery): List<OrderDetailRow>

    @RawQuery
    suspend fun getGroupResults(query: SupportSQLiteQuery): List<GroupResultRow>

    @RawQuery
    suspend fun getAggregateResults(query: SupportSQLiteQuery): List<AggregateResultRow>
}
