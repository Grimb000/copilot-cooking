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
                $COLUMN_APARTMENT TEXT NOT NULL,
                $COLUMN_BIRTH_DATE TEXT NOT NULL DEFAULT '',
                $COLUMN_POSITION CHAR(80) NOT NULL DEFAULT '',
                $COLUMN_RANK CHAR(40) NOT NULL DEFAULT '',
                $COLUMN_SALARY INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_BIRTH_DATE TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_POSITION CHAR(80) NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_RANK CHAR(40) NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_SALARY INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun prepareDemoData() {
        writableDatabase.use { db ->
            if (getCount(db) == 0) {
                seedPeople().forEach { insertPerson(db, it) }
            } else {
                fillMissingExtendedFields(db)
            }
        }
    }

    fun insertAdditionalPerson(): Long {
        val nextIndex = getAllPeople().size + 1
        val person = MilitaryPerson(
            fullName = "Дополнительный военнослужащий $nextIndex",
            postalCode = "2231$nextIndex",
            country = "Беларусь",
            region = "Минская область",
            district = "Минский район",
            city = "Минск",
            street = "Проспект Независимости",
            house = (30 + nextIndex).toString(),
            apartment = (200 + nextIndex).toString(),
            birthDate = "199${nextIndex % 10}-02-1$nextIndex",
            position = "Офицер связи",
            rank = "Капитан",
            salary = 2400 + nextIndex * 50
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
            put(COLUMN_BIRTH_DATE, "1989-11-15")
            put(COLUMN_POSITION, "Начальник отделения")
            put(COLUMN_RANK, "Майор")
            put(COLUMN_SALARY, 3200)
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
                    apartment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_APARTMENT)),
                    birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)),
                    position = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION)),
                    rank = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANK)),
                    salary = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SALARY))
                )
            }
        }
        return result
    }

    private fun fillMissingExtendedFields(db: SQLiteDatabase) {
        db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ID, COLUMN_BIRTH_DATE, COLUMN_POSITION, COLUMN_RANK, COLUMN_SALARY),
            null,
            null,
            null,
            null,
            "$COLUMN_ID ASC"
        ).use { cursor ->
            var index = 0
            while (cursor.moveToNext()) {
                val birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE))
                val position = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION))
                val rank = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RANK))
                val salary = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SALARY))

                if (birthDate.isBlank() || position.isBlank() || rank.isBlank() || salary == 0) {
                    val demo = seedPeople().getOrElse(index) {
                        MilitaryPerson(
                            fullName = "",
                            postalCode = "",
                            country = "",
                            region = "",
                            district = "",
                            city = "",
                            street = "",
                            house = "",
                            apartment = "",
                            birthDate = "1990-01-01",
                            position = "Офицер связи",
                            rank = "Капитан",
                            salary = 2500
                        )
                    }
                    val values = ContentValues().apply {
                        put(COLUMN_BIRTH_DATE, demo.birthDate)
                        put(COLUMN_POSITION, demo.position)
                        put(COLUMN_RANK, demo.rank)
                        put(COLUMN_SALARY, demo.salary)
                    }
                    db.update(
                        TABLE_NAME,
                        values,
                        "$COLUMN_ID = ?",
                        arrayOf(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString())
                    )
                }
                index += 1
            }
        }
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
            put(COLUMN_BIRTH_DATE, person.birthDate)
            put(COLUMN_POSITION, person.position)
            put(COLUMN_RANK, person.rank)
            put(COLUMN_SALARY, person.salary)
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

    private fun getCount(db: SQLiteDatabase): Int {
        db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getInt(0) else 0
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
            apartment = "15",
            birthDate = "1988-04-12",
            position = "Командир взвода",
            rank = "Лейтенант",
            salary = 2100
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
            apartment = "24",
            birthDate = "1985-07-22",
            position = "Заместитель командира роты",
            rank = "Старший лейтенант",
            salary = 2400
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
            apartment = "3",
            birthDate = "1990-01-09",
            position = "Начальник связи",
            rank = "Капитан",
            salary = 2700
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
            apartment = "41",
            birthDate = "1984-10-17",
            position = "Начальник штаба",
            rank = "Майор",
            salary = 3200
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
            apartment = "9",
            birthDate = "1982-12-30",
            position = "Командир роты",
            rank = "Подполковник",
            salary = 3600
        )
    )

    companion object {
        const val DATABASE_NAME = "military_personnel2.db"
        const val DATABASE_VERSION = 2
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
        const val COLUMN_BIRTH_DATE = "birth_date"
        const val COLUMN_POSITION = "position"
        const val COLUMN_RANK = "rank"
        const val COLUMN_SALARY = "salary"
    }
}
