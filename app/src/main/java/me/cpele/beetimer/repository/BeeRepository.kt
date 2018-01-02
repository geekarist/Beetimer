package me.cpele.beetimer.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User
import me.cpele.beetimer.database.CustomDatabase
import me.cpele.beetimer.database.dao.GoalTimingDao
import me.cpele.beetimer.database.dao.StatusChangeDao
import me.cpele.beetimer.domain.GoalTiming
import me.cpele.beetimer.domain.Status
import me.cpele.beetimer.domain.StatusChange
import me.cpele.beetimer.domain.Stopwatch
import me.cpele.beetimer.ui.CustomApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase = Room
            .databaseBuilder(context, CustomDatabase::class.java, context.packageName)
            .build()

    private val goalTimingDao: GoalTimingDao = database.goalTimingDao()
    private val statusChangeDao: StatusChangeDao = database.statusDao()

    val latestStatus: LiveData<StatusChange>
        get() = statusChangeDao.findLatestStatus()
    val goalTimings: LiveData<List<GoalTiming>>
        get() = goalTimingDao.findAll()

    private fun insertOrUpdateGoalTimings(list: List<Goal>, callback: () -> Unit = {}) =
            executor.execute {
                list.map { goal ->
                    val goalTiming =
                            goalTimingDao.findOneBySlug(goal.slug)
                                    ?: GoalTiming(goal = goal, stopwatch = Stopwatch())
                    goalTiming.goal = goal
                    goalTiming
                }.let { goalTimings ->
                    goalTimingDao.insert(goalTimings)
                }
                callback()
            }

    private fun insertStatusChange(status: StatusChange, callback: () -> Unit = {}) =
            executor.execute {
                statusChangeDao.insert(status)
                callback()
            }

    private fun updateGoalTiming(goalTiming: GoalTiming) = executor.execute {
        goalTimingDao.insertOne(goalTiming)
    }

    fun fetch(authToken: String?, callback: () -> Unit = {}) = authToken?.apply { fetchUser(this, callback) }

    private fun fetchUser(accessToken: String, callback: () -> Unit) {

        insertStatusChange(StatusChange(status = Status.LOADING))

        CustomApp.instance.api.getUser(accessToken).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                insertStatusChange(
                        StatusChange(status = Status.failure("Error loading user", t)),
                        callback)
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.apply {
                    fetchGoals(accessToken, username, callback)
                }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String, callback: () -> Unit) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
                insertStatusChange(
                        StatusChange(status = Status.failure("Error loading goals", t)),
                        callback)
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.apply {
                    insertOrUpdateGoalTimings(this) {
                        insertStatusChange(StatusChange(status = Status.SUCCESS), callback)
                    }
                }
            }
        })
    }

    fun persist(goalTiming: GoalTiming) {
        updateGoalTiming(goalTiming)
    }
}

