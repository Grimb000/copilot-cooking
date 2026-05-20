package com.example.task74

data class MilitaryPerson(
    val id: Long = 0,
    val fullName: String,
    val postalCode: String,
    val country: String,
    val region: String,
    val district: String,
    val city: String,
    val street: String,
    val house: String,
    val apartment: String,
    val birthDate: String = "",
    val position: String = "",
    val rank: String = "",
    val salary: Int = 0
) {
    fun toDisplayText(): String {
        return buildString {
            appendLine("ID: $id")
            appendLine("ФИО: $fullName")
            appendLine("Адрес: $postalCode, $country, $region, $district, $city, $street, дом $house, кв. $apartment")
            appendLine("Дата рождения: $birthDate")
            appendLine("Должность: $position")
            appendLine("Звание: $rank")
            appendLine("Зарплата: $salary")
        }.trim()
    }
}
