package me.cpele.fleabrainer.database

import android.arch.persistence.room.TypeConverter
import me.cpele.fleabrainer.domain.Status

class ConvertStatus {

    @TypeConverter
    fun toInteger(status: Status): Int {
        return status.ordinal
    }

    @TypeConverter
    fun fromInteger(ordinal: Int): Status {
        return Status.values().first { it.ordinal == ordinal }
    }
}