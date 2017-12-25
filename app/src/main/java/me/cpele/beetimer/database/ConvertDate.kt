package me.cpele.beetimer.database

import android.arch.persistence.room.TypeConverter
import java.util.*

class ConvertDate {

    @TypeConverter
    fun toMillis(date: Date): Long = date.time

    @TypeConverter
    fun fromMillis(millis: Long) = Date(millis)
}