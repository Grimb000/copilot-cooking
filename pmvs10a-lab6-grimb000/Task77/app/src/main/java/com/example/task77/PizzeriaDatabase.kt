package com.example.task77

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DishEntity::class, PromotionEntity::class, OrderEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PizzeriaDatabase : RoomDatabase() {
    abstract fun dao(): PizzeriaDao

    companion object {
        @Volatile
        private var instance: PizzeriaDatabase? = null

        fun getInstance(context: Context): PizzeriaDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    PizzeriaDatabase::class.java,
                    "pizzeria_room.db"
                ).build().also { instance = it }
            }
        }
    }
}
