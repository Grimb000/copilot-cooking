package com.example.task76

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

class PizzeriaProvider : ContentProvider() {
    private lateinit var dbHelper: PizzeriaDbHelper

    override fun onCreate(): Boolean {
        val appContext = context ?: return false
        dbHelper = PizzeriaDbHelper(appContext)
        dbHelper.ensureSeedData()
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val db = dbHelper.readableDatabase
        val cursor = when (uriMatcher.match(uri)) {
            DISHES -> db.query(PizzeriaContract.TABLE_DISHES, projection, selection, selectionArgs, null, null, sortOrder)
            PROMOTIONS -> db.query(PizzeriaContract.TABLE_PROMOTIONS, projection, selection, selectionArgs, null, null, sortOrder)
            ORDERS -> db.query(PizzeriaContract.TABLE_ORDERS, projection, selection, selectionArgs, null, null, sortOrder)
            ORDER_DETAILS -> db.rawQuery(buildOrderDetailsQuery(sortOrder), null)
            ANALYTICS -> handleAnalyticsQuery(db, uri)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbHelper.writableDatabase
        val (table, baseUri) = when (uriMatcher.match(uri)) {
            DISHES -> PizzeriaContract.TABLE_DISHES to PizzeriaContract.URI_DISHES
            PROMOTIONS -> PizzeriaContract.TABLE_PROMOTIONS to PizzeriaContract.URI_PROMOTIONS
            ORDERS -> PizzeriaContract.TABLE_ORDERS to PizzeriaContract.URI_ORDERS
            else -> throw IllegalArgumentException("Insert is allowed only for base tables.")
        }
        val id = db.insert(table, null, values)
        val resultUri = Uri.withAppendedPath(baseUri, id.toString())
        context?.contentResolver?.notifyChange(uri, null)
        return resultUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        val table = when (uriMatcher.match(uri)) {
            DISHES -> PizzeriaContract.TABLE_DISHES
            PROMOTIONS -> PizzeriaContract.TABLE_PROMOTIONS
            ORDERS -> PizzeriaContract.TABLE_ORDERS
            else -> throw IllegalArgumentException("Delete is allowed only for base tables.")
        }
        val count = db.delete(table, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val db = dbHelper.writableDatabase
        val table = when (uriMatcher.match(uri)) {
            DISHES -> PizzeriaContract.TABLE_DISHES
            PROMOTIONS -> PizzeriaContract.TABLE_PROMOTIONS
            ORDERS -> PizzeriaContract.TABLE_ORDERS
            else -> throw IllegalArgumentException("Update is allowed only for base tables.")
        }
        val count = db.update(table, values, selection, selectionArgs)
        context?.contentResolver?.notifyChange(uri, null)
        return count
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            DISHES, PROMOTIONS, ORDERS, ORDER_DETAILS, ANALYTICS -> "vnd.android.cursor.dir/vnd.${PizzeriaContract.AUTHORITY}"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    private fun handleAnalyticsQuery(db: SQLiteDatabase, uri: Uri): Cursor {
        return when (uri.getQueryParameter("mode")) {
            "aggregate" -> {
                val function = sanitizeAggregateFunction(uri.getQueryParameter("function"))
                db.rawQuery(
                    "SELECT '$function' AS aggregate_function, $function(orders.total_cost) AS aggregate_value FROM ${PizzeriaContract.TABLE_ORDERS} AS orders",
                    null
                )
            }

            "filter" -> {
                val threshold = uri.getQueryParameter("threshold")?.toDoubleOrNull() ?: 0.0
                db.rawQuery(
                    """
                    ${baseOrderDetailsSql()}
                    WHERE orders.total_cost > ?
                    ORDER BY orders.total_cost DESC
                    """.trimIndent(),
                    arrayOf(threshold.toString())
                )
            }

            "group" -> {
                val fieldName = sanitizeGroupField(uri.getQueryParameter("field"))
                val fieldExpression = groupFieldExpression(fieldName)
                db.rawQuery(
                    """
                    SELECT $fieldExpression AS group_value,
                           COUNT(*) AS order_count,
                           SUM(orders.total_cost) AS sum_total_cost,
                           AVG(orders.total_cost) AS avg_total_cost,
                           MIN(orders.total_cost) AS min_total_cost,
                           MAX(orders.total_cost) AS max_total_cost
                    FROM ${PizzeriaContract.TABLE_ORDERS} AS orders
                    INNER JOIN ${PizzeriaContract.TABLE_DISHES} AS dishes ON orders.dish_id = dishes.id
                    LEFT JOIN ${PizzeriaContract.TABLE_PROMOTIONS} AS promotions ON orders.promotion_id = promotions.id
                    GROUP BY $fieldExpression
                    ORDER BY $fieldExpression ASC
                    """.trimIndent(),
                    null
                )
            }

            "sort" -> {
                val field = sanitizeSortField(uri.getQueryParameter("field"))
                val direction = sanitizeDirection(uri.getQueryParameter("direction"))
                db.rawQuery(buildOrderDetailsQuery("$field $direction"), null)
            }

            else -> throw IllegalArgumentException("Unknown analytics mode.")
        }
    }

    private fun buildOrderDetailsQuery(orderBy: String?): String {
        val safeOrderBy = orderBy?.takeIf { it.isNotBlank() } ?: "orders.id ASC"
        return """
            ${baseOrderDetailsSql()}
            ORDER BY $safeOrderBy
        """.trimIndent()
    }

    private fun baseOrderDetailsSql(): String {
        return """
            SELECT orders.id AS order_id,
                   dishes.name AS dish_name,
                   dishes.category AS category,
                   dishes.price AS price,
                   COALESCE(promotions.title, 'Без акции') AS promotion_title,
                   COALESCE(promotions.discount_percent, 0) AS discount_percent,
                   orders.quantity AS quantity,
                   orders.total_cost AS total_cost,
                   orders.delivery_address AS delivery_address,
                   orders.payment_method AS payment_method,
                   orders.courier_name AS courier_name,
                   orders.delivery_method AS delivery_method,
                   orders.order_date AS order_date
            FROM ${PizzeriaContract.TABLE_ORDERS} AS orders
            INNER JOIN ${PizzeriaContract.TABLE_DISHES} AS dishes ON orders.dish_id = dishes.id
            LEFT JOIN ${PizzeriaContract.TABLE_PROMOTIONS} AS promotions ON orders.promotion_id = promotions.id
        """.trimIndent()
    }

    private fun sanitizeAggregateFunction(value: String?): String = when (value?.uppercase()) {
        "SUM", "MIN", "MAX", "COUNT", "AVG" -> value.uppercase()
        else -> "SUM"
    }

    private fun sanitizeGroupField(value: String?): String = when (value) {
        "category", "courier_name", "payment_method", "delivery_method" -> value
        else -> "category"
    }

    private fun groupFieldExpression(field: String): String = when (field) {
        "category" -> "dishes.category"
        "courier_name" -> "orders.courier_name"
        "payment_method" -> "orders.payment_method"
        else -> "orders.delivery_method"
    }

    private fun sanitizeSortField(value: String?): String = when (value) {
        "dish_name" -> "dishes.name"
        "price" -> "dishes.price"
        "total_cost" -> "orders.total_cost"
        "order_date" -> "orders.order_date"
        else -> "orders.id"
    }

    private fun sanitizeDirection(value: String?): String = if (value.equals("DESC", ignoreCase = true)) {
        "DESC"
    } else {
        "ASC"
    }

    companion object {
        private const val DISHES = 1
        private const val PROMOTIONS = 2
        private const val ORDERS = 3
        private const val ORDER_DETAILS = 4
        private const val ANALYTICS = 5

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PizzeriaContract.AUTHORITY, PizzeriaContract.PATH_DISHES, DISHES)
            addURI(PizzeriaContract.AUTHORITY, PizzeriaContract.PATH_PROMOTIONS, PROMOTIONS)
            addURI(PizzeriaContract.AUTHORITY, PizzeriaContract.PATH_ORDERS, ORDERS)
            addURI(PizzeriaContract.AUTHORITY, PizzeriaContract.PATH_ORDER_DETAILS, ORDER_DETAILS)
            addURI(PizzeriaContract.AUTHORITY, PizzeriaContract.PATH_ANALYTICS, ANALYTICS)
        }
    }
}
