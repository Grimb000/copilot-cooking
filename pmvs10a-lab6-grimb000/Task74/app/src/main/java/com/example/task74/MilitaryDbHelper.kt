package com.example.task74

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MilitaryDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_FULL_NAME TEXT NOT NULL,
                $COLUMN_POSTAL_CODE TEXT NOT NULL,
                $COLUMN_COUNTRY TEXT NOT NULL,
                $COLUMN_REGION TEXT NOT NULL,
                $COLUMN_DISTRICT TEXT NOT NULL,
                $COLUMN_CITY TEXT NOT NULL,
                $COLUMN_STREET TEXT NOT NULL,
                $COLUMN_HOUSE TEXT NOT NULL,
                $COLUMN_APARTMENT TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun resetWithSeedData() {
        writableDatabase.use { db ->
            db.delete(TABLE_NAME, null, null)
            seedPeople().forEach { insertPerson(db, it) }
        }
    }

    fun insertAdditionalPerson(): Long {
        val nextIndex = getAllPeople().size + 1
        val person = MilitaryPerson(
            fullName = "Дополнительный военнослужащий $nextIndex",
            postalCode = "2200$nextIndex",
            country = "Беларусь",
            region = "Минская область",
            district = "Минский район",
            city = "Минск",
            street = "Улица Победителей",
            house = (20 + nextIndex).toString(),
            apartment = (100 + nextIndex).toString()
        )
        return writableDatabase.use { db -> insertPerson(db, person) }
    }

    fun replaceFirstPerson(): Int {
        val firstId = getFirstId() ?: return 0
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, "Обновлённый военнослужащий")
            put(COLUMN_POSTAL_CODE, "223050")
            put(COLUMN_COUNTRY, "Беларусь")
            put(COLUMN_REGION, "Брестская область")
            put(COLUMN_DISTRICT, "Брестский район")
            put(COLUMN_CITY, "Брест")
            put(COLUMN_STREET, "Улица Героев")
            put(COLUMN_HOUSE, "7А")
            put(COLUMN_APARTMENT, "12")
        }
        return writableDatabase.use { db ->
            db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(firstId.toString()))
        }
    }

    fun getAllPeople(): List<MilitaryPerson> {
        val result = mutableListOf<MilitaryPerson>()
        readableDatabase.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += MilitaryPerson(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    fullName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FULL_NAME)),
                    postalCode = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSTAL_CODE)),
                    country = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COUNTRY)),
                    region = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REGION)),
                    district = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISTRICT)),
                    city = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CITY)),
                    street = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STREET)),
                    house = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE)),
                    apartment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APARTMENT))
                )
            }
        }
        return result
    }

    private fun insertPerson(db: SQLiteDatabase, person: MilitaryPerson): Long {
        val values = ContentValues().apply {
            put(COLUMN_FULL_NAME, person.fullName)
            put(COLUMN_POSTAL_CODE, person.postalCode)
            put(COLUMN_COUNTRY, person.country)
            put(COLUMN_REGION, person.region)
            put(COLUMN_DISTRICT, person.district)
            put(COLUMN_CITY, person.city)
            put(COLUMN_STREET, person.street)
            put(COLUMN_HOUSE, person.house)
            put(COLUMN_APARTMENT, person.apartment)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    private fun getFirstId(): Long? {
        readableDatabase.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID),
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC",
            "1"
        ).use { cursor ->
            return if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            } else {
                null
            }
        }
    }

    private fun seedPeople(): List<MilitaryPerson> = listOf(
        MilitaryPerson(
            fullName = "Иванов Иван Иванович",
            postalCode = "220030",
            country = "Беларусь",
            region = "Минская область",
            district = "Минский район",
            city = "Минск",
            street = "Улица Ленина",
            house = "10",
            apartment = "15"
        ),
        MilitaryPerson(
            fullName = "Петров Пётр Сергеевич",
            postalCode = "246050",
            country = "Беларусь",
            region = "Гомельская область",
            district = "Гомельский район",
            city = "Гомель",
            street = "Проспект Победы",
            house = "18",
            apartment = "24"
        ),
        MilitaryPerson(
            fullName = "Сидоров Алексей Викторович",
            postalCode = "210026",
            country = "Беларусь",
            region = "Витебская область",
            district = "Витебский район",
            city = "Витебск",
            street = "Улица Советская",
            house = "5",
            apartment = "3"
        ),
        MilitaryPerson(
            fullName = "Кузнецов Андрей Андреевич",
            postalCode = "230023",
            country = "Беларусь",
            region = "Гродненская область",
            district = "Гродненский район",
            city = "Гродно",
            street = "Улица Молодёжная",
            house = "11",
            apartment = "41"
        ),
        MilitaryPerson(
            fullName = "Смирнов Николай Олегович",
            postalCode = "212030",
            country = "Беларусь",
            region = "Могилёвская область",
            district = "Могилёвский район",
            city = "Могилёв",
            street = "Улица Центральная",
            house = "8",
            apartment = "9"
        )
    )

    companion object {
        const val DATABASE_NAME = "military_personnel.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "military_personnel"
        const val COLUMN_ID = "id"
        const val COLUMN_FULL_NAME = "full_name"
        const val COLUMN_POSTAL_CODE = "postal_code"
        const val COLUMN_COUNTRY = "country"
        const val COLUMN_REGION = "region"
        const val COLUMN_DISTRICT = "district"
        const val COLUMN_CITY = "city"
        const val COLUMN_STREET = "street"
        const val COLUMN_HOUSE = "house"
        const val COLUMN_APARTMENT = "apartment"
    }
}
