package com.example.task3

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
    private lateinit var userStore: PersonDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userStore = PersonDataStore(personDataStore)
        setupSaveButton()
        observeData()
    }

    private fun observeData() {
        userStore.userAgeFlow.asLiveData().observe(this) {
            findViewById<TextView>(R.id.tv_age).text = it.toString()
        }

        userStore.userFirstNameFlow.asLiveData().observe(this) {
            findViewById<TextView>(R.id.tv_fname).text = it
        }

        userStore.userLastNameFlow.asLiveData().observe(this) {
            findViewById<TextView>(R.id.tv_lname).text = it
        }

        userStore.userGenderFlow.asLiveData().observe(this) {
            findViewById<TextView>(R.id.tv_gender).text =
                if (it) getString(R.string.gender_male) else getString(R.string.gender_female)
        }
    }

    private fun setupSaveButton() {
        val ageInput = findViewById<EditText>(R.id.et_age)
        val firstNameInput = findViewById<EditText>(R.id.et_fname)
        val lastNameInput = findViewById<EditText>(R.id.et_lname)
        val genderInput = findViewById<SwitchCompat>(R.id.switch_gender)
        val saveButton = findViewById<Button>(R.id.btn_save)
        val statusView = findViewById<TextView>(R.id.tv_gender)

        saveButton.setOnClickListener {
            val firstName = firstNameInput.text.toString().trim()
            val lastName = lastNameInput.text.toString().trim()
            val age = ageInput.text.toString().trim().toIntOrNull()

            if (firstName.isBlank() || lastName.isBlank() || age == null) {
                statusView.text = getString(R.string.validation_error)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                userStore.storeData(age, firstName, lastName, genderInput.isChecked)
            }
        }
    }
}
