package com.example.task77

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val price: Double
)

@Entity(tableName = "promotions")
data class PromotionEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val discountPercent: Int,
    val isActive: Boolean
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val dishId: Int,
    val promotionId: Int?,
    val quantity: Int,
    val totalCost: Double,
    val deliveryAddress: String,
    val paymentMethod: String,
    val courierName: String,
    val deliveryMethod: String,
    val isDelivery: Boolean,
    val orderDate: String
)

data class OrderDetailRow(
    val order_id: Int,
    val dish_name: String,
    val category: String,
    val price: Double,
    val promotion_title: String,
    val discount_percent: Int,
    val quantity: Int,
    val total_cost: Double,
    val delivery_address: String,
    val payment_method: String,
    val courier_name: String,
    val delivery_method: String,
    val order_date: String
)

data class GroupResultRow(
    val group_value: String,
    val order_count: Int,
    val sum_total_cost: Double,
    val avg_total_cost: Double,
    val min_total_cost: Double,
    val max_total_cost: Double
)

data class AggregateResultRow(
    val aggregate_function: String,
    val aggregate_value: Double
)
