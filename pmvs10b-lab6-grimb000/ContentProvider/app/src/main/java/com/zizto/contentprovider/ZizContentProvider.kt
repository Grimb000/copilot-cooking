package com.zizto.contentprovider


import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class ZizContentProvider : ContentProvider() {

    companion object {
        const val PROVIDER_NAME = "com.example.contentprovider.provider"
        const val URL = "content://$PROVIDER_NAME/users"
        val CONTENT_URI: Uri = Uri.parse(URL)

        const val ID = "id"
        const val NAME = "name"
        const val DEPT = "department"

        const val uriCode = 1
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(PROVIDER_NAME, "users", uriCode)
        }

        const val DATABASE_NAME = "CompanyDB"
        const val TABLE_NAME = "users"
        const val DATABASE_VERSION = 1
        const val CREATE_DB_TABLE = ("CREATE TABLE " + TABLE_NAME
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + " name TEXT NOT NULL, "
                + " department TEXT NOT NULL);")
    }

    private var db: SQLiteDatabase? = null

    private class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context
        val dbHelper = DatabaseHelper(context)
        db = dbHelper.writableDatabase
        return db != null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val cursor = db?.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db?.insert(TABLE_NAME, "", values)
        if (rowID != null && rowID > 0) {
            val newUri = Uri.withAppendedPath(CONTENT_URI, rowID.toString())
            context?.contentResolver?.notifyChange(newUri, null)
            return newUri
        }
        return null
    }
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.example.users"
}