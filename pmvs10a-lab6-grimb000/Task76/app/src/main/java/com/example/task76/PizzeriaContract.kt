package com.example.task76

import android.net.Uri

object PizzeriaContract {
    const val AUTHORITY = "com.example.task76.provider"
    private const val SCHEME = "content"

    const val TABLE_DISHES = "dishes"
    const val TABLE_PROMOTIONS = "promotions"
    const val TABLE_ORDERS = "orders"

    const val PATH_DISHES = "dishes"
    const val PATH_PROMOTIONS = "promotions"
    const val PATH_ORDERS = "orders"
    const val PATH_ORDER_DETAILS = "order_details"
    const val PATH_ANALYTICS = "analytics"

    val URI_DISHES: Uri = Uri.parse("$SCHEME://$AUTHORITY/$PATH_DISHES")
    val URI_PROMOTIONS: Uri = Uri.parse("$SCHEME://$AUTHORITY/$PATH_PROMOTIONS")
    val URI_ORDERS: Uri = Uri.parse("$SCHEME://$AUTHORITY/$PATH_ORDERS")
    val URI_ORDER_DETAILS: Uri = Uri.parse("$SCHEME://$AUTHORITY/$PATH_ORDER_DETAILS")
    val URI_ANALYTICS: Uri = Uri.parse("$SCHEME://$AUTHORITY/$PATH_ANALYTICS")
}
