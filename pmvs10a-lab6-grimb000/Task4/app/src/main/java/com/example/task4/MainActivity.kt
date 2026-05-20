package com.example.task4

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: ContactsDbHelper
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var outputView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ContactsDbHelper(this)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        outputView = findViewById(R.id.outputView)

        findViewById<Button>(R.id.addButton).setOnClickListener { addContact() }
        findViewById<Button>(R.id.readButton).setOnClickListener { readContacts() }
        findViewById<Button>(R.id.clearButton).setOnClickListener { clearContacts() }

        readContacts()
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }

    private fun addContact() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        if (name.isBlank() || email.isBlank()) {
            Toast.makeText(this, R.string.enter_name_and_email, Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(ContactsDbHelper.COLUMN_NAME, name)
            put(ContactsDbHelper.COLUMN_EMAIL, email)
        }

        val rowId = dbHelper.writableDatabase.insert(ContactsDbHelper.TABLE_CONTACTS, null, values)
        Log.d(LOG_TAG, "row inserted, ID = $rowId, name = $name, email = $email")

        nameInput.text.clear()
        emailInput.text.clear()
        readContacts()
    }

    private fun readContacts() {
        val cursor = dbHelper.readableDatabase.query(
            ContactsDbHelper.TABLE_CONTACTS,
            null,
            null,
            null,
            null,
            null,
            "${ContactsDbHelper.COLUMN_ID} ASC"
        )

        outputView.text = cursor.toFormattedLog()
        cursor.close()
    }

    private fun clearContacts() {
        val deletedRows = dbHelper.writableDatabase.delete(ContactsDbHelper.TABLE_CONTACTS, null, null)
        Log.d(LOG_TAG, "deleted rows count = $deletedRows")
        outputView.text = getString(R.string.cleared_message, deletedRows)
    }

    private fun Cursor.toFormattedLog(): String {
        if (!moveToFirst()) {
            Log.d(LOG_TAG, "0 rows")
            return this@MainActivity.getString(R.string.no_contacts)
        }

        val result = StringBuilder()
        val idIndex = getColumnIndexOrThrow(ContactsDbHelper.COLUMN_ID)
        val nameIndex = getColumnIndexOrThrow(ContactsDbHelper.COLUMN_NAME)
        val emailIndex = getColumnIndexOrThrow(ContactsDbHelper.COLUMN_EMAIL)

        do {
            val line = "ID = ${getInt(idIndex)}, name = ${getString(nameIndex)}, email = ${getString(emailIndex)}"
            Log.d(LOG_TAG, line)
            result.appendLine(line)
        } while (moveToNext())

        return result.toString().trim()
    }

    companion object {
        private const val LOG_TAG = "Task4Contacts"
    }
}
