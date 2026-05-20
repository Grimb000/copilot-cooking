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
    val apartment: String
) {
    fun toDisplayText(): String {
        return buildString {
            appendLine("ID: $id")
            appendLine("ФИО: $fullName")
            appendLine("Адрес: $postalCode, $country, $region, $district, $city, $street, дом $house, кв. $apartment")
        }.trim()
    }
}
