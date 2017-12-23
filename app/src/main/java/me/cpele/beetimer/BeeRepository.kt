package me.cpele.beetimer

import android.arch.persistence.room.Room
import android.content.Context
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User
import me.cpele.beetimer.database.CustomDatabase
import me.cpele.beetimer.database.GoalDao
import me.cpele.beetimer.database.UserDao
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase =
            Room.databaseBuilder(context, CustomDatabase::class.java, context.packageName).build()
    private val userDao: UserDao
    private val goalDao: GoalDao

    init {
        userDao = database.userDao()
        goalDao = database.goalDao()
    }

    fun insertOrUpdateUser(user: User) {
        executor.execute {
            userDao.insertOrUpdate(user)
        }
    }

    fun insertOrUpdateGoals(list: List<Goal>) {
        executor.execute {
            goalDao.insertOrUpdate(list)
        }
    }

}