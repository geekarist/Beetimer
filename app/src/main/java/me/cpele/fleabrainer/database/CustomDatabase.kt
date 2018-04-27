package me.cpele.fleabrainer.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import me.cpele.fleabrainer.database.dao.DatapointDao
import me.cpele.fleabrainer.database.dao.GoalTimingDao
import me.cpele.fleabrainer.database.dao.StatusChangeDao
import me.cpele.fleabrainer.domain.DatapointBo
import me.cpele.fleabrainer.domain.GoalTiming
import me.cpele.fleabrainer.domain.StatusChange

@Database(entities = [GoalTiming::class, StatusChange::class, DatapointBo::class], version = 3)
@TypeConverters(ConvertStatus::class, ConvertDate::class)
abstract class CustomDatabase : RoomDatabase() {
    abstract fun goalTimingDao(): GoalTimingDao
    abstract fun statusDao(): StatusChangeDao
    abstract fun datapointDao(): DatapointDao
}