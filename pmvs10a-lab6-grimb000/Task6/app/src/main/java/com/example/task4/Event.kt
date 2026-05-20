package com.example.task6

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventTable")
data class Event(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val id: Int = 0,
    @ColumnInfo(name = "event_title")
    val title: String,
    @ColumnInfo(name = "event_description")
    val description: String
)
