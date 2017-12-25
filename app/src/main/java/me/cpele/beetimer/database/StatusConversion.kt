package me.cpele.beetimer.database

import android.arch.persistence.room.TypeConverter
import me.cpele.beetimer.domain.Status

class StatusConversion {

    @TypeConverter
    fun toInteger(status: Status): Int {
        return status.ordinal
    }

    @TypeConverter
    fun fromInteger(ordinal: Int): Status {
        return Status.values().first { it.ordinal == ordinal }
    }
}