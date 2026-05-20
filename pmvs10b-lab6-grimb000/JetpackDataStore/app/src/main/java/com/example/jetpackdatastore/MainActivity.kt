package com.example.jetpackdatastore

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import androidx.lifecycle.asLiveData
import androidx.lifecycle.observe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var userManager: UserManager
    var age = 0
    var fname = ""
    var lname = ""
    var gender = ""

    private lateinit var tv_gender: TextView
    private lateinit var tv_age: TextView
    private lateinit var tv_fname: TextView
    private lateinit var tv_lname: TextView

    private lateinit var btn_save: Button
    private lateinit var et_fname: EditText
    private lateinit var et_lname: EditText
    private lateinit var et_age: EditText
    private lateinit var switch_gender: SwitchCompat


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_gender = findViewById<TextView>(R.id.tv_gender)
        tv_age = findViewById<TextView>(R.id.tv_age)
        tv_fname = findViewById<TextView>(R.id.tv_fname)
        tv_lname = findViewById<TextView>(R.id.tv_lname)

        btn_save = findViewById<Button>(R.id.btn_save)
        et_fname = findViewById<EditText>(R.id.et_fname)
        et_lname = findViewById<EditText>(R.id.et_lname)
        et_age = findViewById<EditText>(R.id.et_age)
        switch_gender = findViewById<SwitchCompat>(R.id.switch_gender)

        userManager = UserManager(dataStore)

        buttonSave()

        observeData()
    }

    private fun observeData() {

        userManager.userAgeFlow.asLiveData().observe(this, {
            if (it != null) {
                age = it
                tv_age.text = it.toString()
            }
        })

        userManager.userFirstNameFlow.asLiveData().observe(this, {
            if (it != null) {
                fname = it
                tv_fname.text = it
            }
        })

        userManager.userLastNameFlow.asLiveData().observe(this, {
            if (it != null) {
                lname = it
                tv_lname.text = it
            }
        })

        userManager.userGenderFlow.asLiveData().observe(this, {
            if (it != null) {
                gender = if (it) "Male" else "Female"
                tv_gender.text = gender
            }
        })
    }

    private fun buttonSave() {
        btn_save.setOnClickListener {
            fname = et_fname.text.toString()
            lname = et_lname.text.toString()
            age = et_age.text.toString().toInt()
            val isMale = switch_gender.isChecked

            GlobalScope.launch {
                userManager.storeUser(age, fname, lname, isMale)
            }
        }
    }
}