package me.cpele.beetimer

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Goal::class, User::class], version = 1)
abstract class CustomDatabase: RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun goalDao(): GoalDao
}