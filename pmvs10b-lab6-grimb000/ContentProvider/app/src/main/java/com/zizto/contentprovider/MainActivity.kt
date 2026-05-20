package com.zizto.contentprovider

import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val etDept = findViewById<EditText>(R.id.etDept)
        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val btnLoad = findViewById<Button>(R.id.btnLoad)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnAdd.setOnClickListener {
            val values = ContentValues().apply {
                put(ZizContentProvider.NAME, etName.text.toString())
                put(ZizContentProvider.DEPT, etDept.text.toString())
            }

            contentResolver.insert(ZizContentProvider.CONTENT_URI, values)
            Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()

            etName.text.clear()
            etDept.text.clear()
        }

        btnLoad.setOnClickListener {
            val cursor = contentResolver.query(ZizContentProvider.CONTENT_URI, null, null, null, null)
            val resultText = StringBuilder()

            if (cursor != null && cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(ZizContentProvider.ID)
                val nameIndex = cursor.getColumnIndex(ZizContentProvider.NAME)
                val deptIndex = cursor.getColumnIndex(ZizContentProvider.DEPT)

                do {
                    resultText.append("ID: ${cursor.getInt(idIndex)}\n")
                    resultText.append("Имя: ${cursor.getString(nameIndex)}\n")
                    resultText.append("Отдел: ${cursor.getString(deptIndex)}\n\n")
                } while (cursor.moveToNext())
                cursor.close()
            } else {
                resultText.append("Записей не найдено")
            }

            tvResult.text = resultText.toString()
        }
    }
}