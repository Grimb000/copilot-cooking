package com.example.task74

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var dbHelper: MilitaryDbHelper
    private lateinit var statusView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = MilitaryDbHelper(this)
        statusView = findViewById(R.id.statusView)

        dbHelper.resetWithSeedData()
        showStatus(getString(R.string.db_initialized, dbHelper.getAllPeople().size))

        findViewById<Button>(R.id.showRecordsButton).setOnClickListener {
            startActivity(Intent(this, RecordsActivity::class.java))
        }
        findViewById<Button>(R.id.addRecordButton).setOnClickListener {
            val rowId = dbHelper.insertAdditionalPerson()
            showStatus(getString(R.string.record_added, rowId, dbHelper.getAllPeople().size))
        }
        findViewById<Button>(R.id.replaceRecordButton).setOnClickListener {
            val updatedRows = dbHelper.replaceFirstPerson()
            showStatus(getString(R.string.record_replaced, updatedRows))
        }
    }

    private fun showStatus(message: String) {
        statusView.text = message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
