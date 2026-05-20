package com.example.task5

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var outputView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        outputView = findViewById(R.id.outputView)

        findViewById<Button>(R.id.addButton).setOnClickListener { addContact() }
        findViewById<Button>(R.id.readButton).setOnClickListener { readContacts() }
        findViewById<Button>(R.id.clearButton).setOnClickListener { clearContacts() }

        readContacts()
    }

    private fun addContact() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()

        if (name.isBlank() || email.isBlank()) {
            Toast.makeText(this, R.string.enter_name_and_email, Toast.LENGTH_SHORT).show()
            return
        }

        val values = ContentValues().apply {
            put(ContactsContract.Columns.NAME, name)
            put(ContactsContract.Columns.EMAIL, email)
        }

        val insertedUri = contentResolver.insert(ContactsContract.CONTENT_URI, values)
        Log.d(LOG_TAG, "Inserted: $insertedUri")

        nameInput.text.clear()
        emailInput.text.clear()
        readContacts()
    }

    private fun readContacts() {
        val cursor = contentResolver.query(
            ContactsContract.CONTENT_URI,
            null,
            null,
            null,
            "${BaseColumns._ID} ASC"
        )

        outputView.text = cursor?.toFormattedLog() ?: getString(R.string.no_contacts)
        cursor?.close()
    }

    private fun clearContacts() {
        val deletedRows = contentResolver.delete(ContactsContract.CONTENT_URI, null, null)
        Log.d(LOG_TAG, "Deleted rows: $deletedRows")
        outputView.text = getString(R.string.cleared_message, deletedRows)
    }

    private fun Cursor.toFormattedLog(): String {
        if (!moveToFirst()) {
            return this@MainActivity.getString(R.string.no_contacts)
        }

        val idIndex = getColumnIndexOrThrow(BaseColumns._ID)
        val nameIndex = getColumnIndexOrThrow(ContactsContract.Columns.NAME)
        val emailIndex = getColumnIndexOrThrow(ContactsContract.Columns.EMAIL)
        val lines = StringBuilder()

        do {
            val line = "ID = ${getInt(idIndex)}, name = ${getString(nameIndex)}, email = ${getString(emailIndex)}"
            Log.d(LOG_TAG, line)
            lines.appendLine(line)
        } while (moveToNext())

        return lines.toString().trim()
    }

    companion object {
        private const val LOG_TAG = "Task5Provider"
    }
}
