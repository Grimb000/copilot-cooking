package com.example.task5

import android.net.Uri
import android.provider.BaseColumns

object ContactsContract {
    const val AUTHORITY = "com.example.task5.provider"
    private const val BASE_PATH = "contacts"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")

    object Columns : BaseColumns {
        const val TABLE_NAME = "contacts"
        const val NAME = "name"
        const val EMAIL = "email"
    }
}
