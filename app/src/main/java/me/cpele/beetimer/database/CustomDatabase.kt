package me.cpele.beetimer.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User

@Database(entities = [Goal::class, User::class], version = 1)
abstract class CustomDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
}