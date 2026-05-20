package com.example.task2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var userManager: UserManager
    private var age = 0
    private var fname = ""
    private var lname = ""
    private var gender = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userManager = UserManager(dataStore)
        buttonSave()
        observeData()
    }

    private fun observeData() {
        userManager.userAgeFlow.asLiveData().observe(this, {
            if (it != null) {
                age = it
                findViewById<TextView>(R.id.tv_age).text = it.toString()
            }
        })

        userManager.userFirstNameFlow.asLiveData().observe(this, {
            if (it != null) {
                fname = it
                findViewById<TextView>(R.id.tv_fname).text = it
            }
        })

        userManager.userLastNameFlow.asLiveData().observe(this, {
            if (it != null) {
                lname = it
                findViewById<TextView>(R.id.tv_lname).text = it
            }
        })

        userManager.userGenderFlow.asLiveData().observe(this, {
            if (it != null) {
                gender = if (it) getString(R.string.gender_male) else getString(R.string.gender_female)
                findViewById<TextView>(R.id.tv_gender).text = gender
            }
        })
    }

    private fun buttonSave() {
        val etAge = findViewById<EditText>(R.id.et_age)
        val etFname = findViewById<EditText>(R.id.et_fname)
        val etLname = findViewById<EditText>(R.id.et_lname)
        val switch_gender = findViewById<SwitchCompat>(R.id.switch_gender)
        val btn_save = findViewById<Button>(R.id.btn_save)

        btn_save.setOnClickListener {
            fname = etFname.text.toString().trim()
            lname = etLname.text.toString().trim()
            val ageValue = etAge.text.toString().trim()

            if (fname.isBlank() || lname.isBlank() || ageValue.isBlank()) {
                findViewById<TextView>(R.id.tv_gender).text = getString(R.string.validation_error)
                return@setOnClickListener
            }

            age = ageValue.toIntOrNull() ?: run {
                findViewById<TextView>(R.id.tv_gender).text = getString(R.string.validation_error)
                return@setOnClickListener
            }

            val isMale = switch_gender.isChecked

            lifecycleScope.launch {
                userManager.storeUser(age, fname, lname, isMale)
            }
        }
    }
}
