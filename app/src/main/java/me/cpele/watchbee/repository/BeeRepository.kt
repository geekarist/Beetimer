package me.cpele.watchbee.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import me.cpele.watchbee.api.Datapoint
import me.cpele.watchbee.api.Goal
import me.cpele.watchbee.api.User
import me.cpele.watchbee.database.CustomDatabase
import me.cpele.watchbee.database.dao.GoalTimingDao
import me.cpele.watchbee.database.dao.PendingDatapointDao
import me.cpele.watchbee.database.dao.StatusChangeDao
import me.cpele.watchbee.domain.*
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
    private val pendingDatapointDao: PendingDatapointDao = database.pendingDatapointDao()

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

    fun fetch(authToken: String?, callback: () -> Unit = {}) {
        authToken?.apply { fetchUser(this, callback) }
    }

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
                    postQueuedDatapoints(accessToken, username) {
                        fetchGoals(accessToken, username, callback)
                    }
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

        val userName = goalTiming.user
        val goalSlug = goalTiming.goal.slug
        val datapointValue = goalTiming.stopwatch.elapsedDecimalMinutes
        val comment = "via WatchBee at ${Date()}"
        val accessToken = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(SignInActivity.PREF_ACCESS_TOKEN, null)

        postDatapoint(userName, goalSlug, datapointValue, comment, accessToken, goalTiming)
    }

    private fun postDatapoint(
            userName: String,
            goalSlug: String,
            datapointValue: Float,
            comment: String,
            accessToken: String,
            goalTiming: GoalTiming,
            indicateStatusChange: Boolean = true
    ) {
        if (indicateStatusChange) insertStatusChange(StatusChange(status = Status.LOADING))

        CustomApp.instance.api
                .postDatapoint(userName, goalSlug, datapointValue, comment, accessToken)
                .enqueue(object : Callback<Datapoint> {

                    override fun onFailure(call: Call<Datapoint>?, t: Throwable?) {
                        val tag = BeeRepository::class.java.simpleName
                        Log.e(tag, "Error posting goal timing: ", t)
                        if (indicateStatusChange) {
                            insertStatusChange(StatusChange(status = Status.FAILURE))
                        }
                        enqueueDatapoint(userName, goalSlug, datapointValue, comment)
                    }

                    override fun onResponse(call: Call<Datapoint>?, response: Response<Datapoint>?) {
                        goalTiming.stopwatch.clear()
                        persist(goalTiming)
                        if (indicateStatusChange) {
                            insertStatusChange(StatusChange(status = Status.SUCCESS))
                        }
                    }
                })
    }

    private fun enqueueDatapoint(
            userName: String,
            goalSlug: String,
            datapointValue: Float,
            comment: String
    ) {
        executor.execute {
            val datapoint = DatapointBo(
                    userName = userName,
                    goalSlug = goalSlug,
                    datapointValue = datapointValue,
                    comment = comment
            )
            pendingDatapointDao.insertOne(datapoint)
        }
    }

    private fun postQueuedDatapoints(accessToken: String, userName: String, callback: () -> Unit) {
        executor.execute {
            val pendingDatapoints = pendingDatapointDao.findAll(userName)
            Log.d(javaClass.simpleName, "Found pending datapoints: $pendingDatapoints")
            pendingDatapoints.forEach { datapoint ->
                pendingDatapointDao.deleteOne(datapoint)
                val goalTiming = goalTimingDao.findOneBySlug(datapoint.goalSlug)
                goalTiming?.let { gt ->
                    postDatapoint(
                            userName,
                            datapoint.goalSlug,
                            datapoint.datapointValue,
                            datapoint.comment,
                            accessToken,
                            goalTiming = gt,
                            indicateStatusChange = false
                    )
                }
            }
            callback()
        }
    }
}

