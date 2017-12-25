package me.cpele.beetimer.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User

@Database(entities = [Goal::class, User::class, StatusContainer::class], version = 1)
@TypeConverters(StatusConversion::class)
abstract class CustomDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
    abstract fun statusDao(): StatusDao
}