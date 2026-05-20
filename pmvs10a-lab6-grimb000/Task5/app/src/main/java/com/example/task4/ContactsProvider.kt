package com.example.task5

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.provider.BaseColumns

class ContactsProvider : ContentProvider() {
    private lateinit var dbHelper: ContactsDbHelper

    override fun onCreate(): Boolean {
        dbHelper = ContactsDbHelper(requireNotNull(context))
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val database = dbHelper.readableDatabase
        val cursor = when (uriMatcher.match(uri)) {
            CONTACTS -> database.query(
                ContactsContract.Columns.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder ?: "${BaseColumns._ID} ASC"
            )

            CONTACT_ID -> {
                val id = uri.lastPathSegment ?: error("Missing id")
                database.query(
                    ContactsContract.Columns.TABLE_NAME,
                    projection,
                    "${BaseColumns._ID}=?",
                    arrayOf(id),
                    null,
                    null,
                    sortOrder
                )
            }

            else -> error("Unknown URI: $uri")
        }

        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String = when (uriMatcher.match(uri)) {
        CONTACTS -> "vnd.android.cursor.dir/vnd.${ContactsContract.AUTHORITY}.contacts"
        CONTACT_ID -> "vnd.android.cursor.item/vnd.${ContactsContract.AUTHORITY}.contacts"
        else -> error("Unknown URI: $uri")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        require(uriMatcher.match(uri) == CONTACTS) { "Invalid URI for insert: $uri" }
        val id = dbHelper.writableDatabase.insertOrThrow(
            ContactsContract.Columns.TABLE_NAME,
            null,
            values
        )
        val insertedUri = ContentUris.withAppendedId(ContactsContract.CONTENT_URI, id)
        context?.contentResolver?.notifyChange(insertedUri, null)
        return insertedUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val deleted = when (uriMatcher.match(uri)) {
            CONTACTS -> dbHelper.writableDatabase.delete(
                ContactsContract.Columns.TABLE_NAME,
                selection,
                selectionArgs
            )

            CONTACT_ID -> {
                val id = uri.lastPathSegment ?: error("Missing id")
                dbHelper.writableDatabase.delete(
                    ContactsContract.Columns.TABLE_NAME,
                    "${BaseColumns._ID}=?",
                    arrayOf(id)
                )
            }

            else -> error("Unknown URI: $uri")
        }

        if (deleted > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return deleted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val updated = when (uriMatcher.match(uri)) {
            CONTACTS -> dbHelper.writableDatabase.update(
                ContactsContract.Columns.TABLE_NAME,
                values,
                selection,
                selectionArgs
            )

            CONTACT_ID -> {
                val id = uri.lastPathSegment ?: error("Missing id")
                dbHelper.writableDatabase.update(
                    ContactsContract.Columns.TABLE_NAME,
                    values,
                    "${BaseColumns._ID}=?",
                    arrayOf(id)
                )
            }

            else -> error("Unknown URI: $uri")
        }

        if (updated > 0) {
            context?.contentResolver?.notifyChange(uri, null)
        }
        return updated
    }

    companion object {
        private const val CONTACTS = 1
        private const val CONTACT_ID = 2

        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(ContactsContract.AUTHORITY, "contacts", CONTACTS)
            addURI(ContactsContract.AUTHORITY, "contacts/#", CONTACT_ID)
        }
    }
}
