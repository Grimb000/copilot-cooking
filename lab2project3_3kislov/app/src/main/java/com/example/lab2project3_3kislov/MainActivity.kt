package com.example.lab2project3_3kislov

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var etInput: EditText
    private lateinit var tvResultType: TextView
    private lateinit var tvError: TextView
    private lateinit var btnCheck: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        etInput = findViewById(R.id.etInput)
        tvResultType = findViewById(R.id.tvResultType)
        tvError = findViewById(R.id.tvError)
        btnCheck = findViewById(R.id.btnCheck)

        btnCheck.setOnClickListener(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars: Insets =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.btnCheck) {
            checkType()
        }
    }

    private fun checkType() {
        tvError.text = ""
        tvResultType.text = ""

        val s = etInput.text.toString().trim()
        if (s.isEmpty()) {
            tvError.setText(R.string.error_empty)
            return
        }


        val isInteger = s.toLongOrNull() != null
        val intValue = s.toLongOrNull()
        val doubleValue = s.toDoubleOrNull()

        if (!isInteger && doubleValue == null) {
            tvError.setText(R.string.error_not_number)
            return
        }

        if (doubleValue != null && (doubleValue > Double.MAX_VALUE || doubleValue < -Double.MAX_VALUE)) {
            tvError.setText(R.string.error_out_of_range)
            return
        }


        if (doubleValue != null && doubleValue % 1.0 != 0.0) {

            val floatVal = doubleValue.toFloat()
            val fitsInFloat = floatVal.toDouble() == doubleValue

            val res = if (fitsInFloat) {
                getString(R.string.type_float)
            } else {
                getString(R.string.type_double)
            }
            tvResultType.text = getString(R.string.result_type, res)
            return
        }


        if (intValue == null) {
            tvError.setText(R.string.error_out_of_range)
            return
        }

        val n = intValue

        val resType = when {
            n in Byte.MIN_VALUE..Byte.MAX_VALUE -> getString(R.string.type_byte)
            n in Short.MIN_VALUE..Short.MAX_VALUE -> getString(R.string.type_short)
            n in Int.MIN_VALUE..Int.MAX_VALUE -> getString(R.string.type_int)
            else -> getString(R.string.type_long)
        }

        tvResultType.text = getString(R.string.result_type, resType)
    }
}
