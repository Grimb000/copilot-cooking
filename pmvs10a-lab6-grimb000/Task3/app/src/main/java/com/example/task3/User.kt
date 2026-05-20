package com.example.task3

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore

val Context.personDataStore: DataStore<Person> by dataStore(
    fileName = "user.pb",
    serializer = PersonSerializer
)
