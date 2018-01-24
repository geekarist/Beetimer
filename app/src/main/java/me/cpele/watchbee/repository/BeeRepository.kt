package me.cpele.watchbee.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import me.cpele.watchbee.api.Datapoint
import me.cpele.watchbee.api.Goal
import me.cpele.watchbee.api.User
import me.cpele.watchbee.database.CustomDatabase
import me.cpele.watchbee.database.dao.GoalTimingDao
import me.cpele.watchbee.database.dao.StatusChangeDao
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.Status
import me.cpele.watchbee.domain.StatusChange
import me.cpele.watchbee.domain.Stopwatch
import me.cpele.watchbee.ui.CustomApp
import me.cpele.watchbee.ui.SignInActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase = Room
            .databaseBuilder(context, CustomDatabase::class.java, context.packageName)
            .build()

    private val goalTimingDao: GoalTimingDao = database.goalTimingDao()
    private val statusChangeDao: StatusChangeDao = database.statusDao()

    val latestStatus: LiveData<StatusChange> get() = statusChangeDao.findLatestStatus()
    val goalTimings: LiveData<List<GoalTiming>> get() = goalTimingDao.findAll()

    private fun insertOrUpdateGoalTimings(user: String, list: List<Goal>, callback: () -> Unit = {}) =
            executor.execute {
                list.map { goal ->
                    val goalTiming =
                            goalTimingDao.findOneBySlug(goal.slug)
                                    ?: GoalTiming(user = user, goal = goal, stopwatch = Stopwatch())
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
                    insertOrUpdateGoalTimings(user, this) {
                        insertStatusChange(StatusChange(status = Status.SUCCESS), callback)
                    }
                }
            }
        })
    }

    fun persist(goalTiming: GoalTiming) {
        updateGoalTiming(goalTiming)
    }

    fun submit(context: Context, goalTiming: GoalTiming) {

        val userName = "chrp"
        val goalSlug = goalTiming.goal.slug
        val datapointValue = goalTiming.stopwatch.elapsedDecimalMinutes
        val comment = "via WatchBee at ${Date()}"
        val accessToken = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SignInActivity.PREF_ACCESS_TOKEN, null)

        CustomApp.instance.api
                .postDatapoint(userName, goalSlug, datapointValue, comment, accessToken)
                .enqueue(object: Callback<Datapoint> {

                    override fun onFailure(call: Call<Datapoint>?, t: Throwable?) {
                        Toast.makeText(context, "Failure", Toast.LENGTH_LONG).show()
                        val tag = BeeRepository::class.java.simpleName
                        Log.e(tag, "Error posting goal timing: ", t)
                    }

                    override fun onResponse(call: Call<Datapoint>?, response: Response<Datapoint>?) {
                        val msg = "Response body: ${response?.body()}"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        goalTiming.stopwatch.clear()
                        persist(goalTiming)
                    }
                })
    }
}

