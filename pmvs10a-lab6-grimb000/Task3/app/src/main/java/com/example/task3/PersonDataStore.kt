package com.example.task3

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PersonDataStore(private val dataStore: DataStore<Person>) {
    val user: Flow<Person> = dataStore.data

    suspend fun storeData(age: Int, fname: String, lname: String, isMale: Boolean) {
        dataStore.updateData { current ->
            current.toBuilder()
                .setAge(age)
                .setFirstName(fname)
                .setLastName(lname)
                .setGender(isMale)
                .build()
        }
    }

    val userAgeFlow: Flow<Int> = user.map { it.age }
    val userFirstNameFlow: Flow<String> = user.map { it.firstName }
    val userLastNameFlow: Flow<String> = user.map { it.lastName }
    val userGenderFlow: Flow<Boolean> = user.map { it.gender }
}
