package com.example.task77

object QuerySqlBuilders {
    fun orderDetails(orderBy: String = "orders.id ASC"): String {
        return """
            ${baseOrderDetails()}
            ORDER BY $orderBy
        """.trimIndent()
    }

    fun aggregate(function: String): String {
        val safeFunction = when (function.uppercase()) {
            "SUM", "MIN", "MAX", "COUNT", "AVG" -> function.uppercase()
            else -> "SUM"
        }
        val expression = if (safeFunction == "COUNT") {
            "CAST(COUNT(orders.totalCost) AS REAL)"
        } else {
            "$safeFunction(orders.totalCost)"
        }
        return "SELECT '$safeFunction' AS aggregate_function, $expression AS aggregate_value FROM orders AS orders"
    }

    fun filter(threshold: Double): String {
        return """
            ${baseOrderDetails()}
            WHERE orders.totalCost > $threshold
            ORDER BY orders.totalCost DESC
        """.trimIndent()
    }

    fun group(field: String): String {
        val expression = when (field) {
            "category" -> "dishes.category"
            "courier_name" -> "orders.courierName"
            "payment_method" -> "orders.paymentMethod"
            else -> "orders.deliveryMethod"
        }
        return """
            SELECT $expression AS group_value,
                   COUNT(*) AS order_count,
                   SUM(orders.totalCost) AS sum_total_cost,
                   AVG(orders.totalCost) AS avg_total_cost,
                   MIN(orders.totalCost) AS min_total_cost,
                   MAX(orders.totalCost) AS max_total_cost
            FROM orders AS orders
            INNER JOIN dishes AS dishes ON orders.dishId = dishes.id
            LEFT JOIN promotions AS promotions ON orders.promotionId = promotions.id
            GROUP BY $expression
            ORDER BY $expression ASC
        """.trimIndent()
    }

    fun sort(field: String, direction: String): String {
        val safeField = when (field) {
            "dish_name" -> "dishes.name"
            "price" -> "dishes.price"
            "total_cost" -> "orders.totalCost"
            "order_date" -> "orders.orderDate"
            else -> "orders.id"
        }
        val safeDirection = if (direction.equals("DESC", ignoreCase = true)) "DESC" else "ASC"
        return orderDetails("$safeField $safeDirection")
    }

    private fun baseOrderDetails(): String {
        return """
            SELECT orders.id AS order_id,
                   dishes.name AS dish_name,
                   dishes.category AS category,
                   dishes.price AS price,
                   COALESCE(promotions.title, 'Без акции') AS promotion_title,
                   COALESCE(promotions.discountPercent, 0) AS discount_percent,
                   orders.quantity AS quantity,
                   orders.totalCost AS total_cost,
                   orders.deliveryAddress AS delivery_address,
                   orders.paymentMethod AS payment_method,
                   orders.courierName AS courier_name,
                   orders.deliveryMethod AS delivery_method,
                   orders.orderDate AS order_date
            FROM orders AS orders
            INNER JOIN dishes AS dishes ON orders.dishId = dishes.id
            LEFT JOIN promotions AS promotions ON orders.promotionId = promotions.id
        """.trimIndent()
    }
}
