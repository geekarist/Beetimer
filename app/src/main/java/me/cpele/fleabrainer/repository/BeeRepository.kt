package me.cpele.fleabrainer.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.util.Log
import me.cpele.fleabrainer.api.Datapoint
import me.cpele.fleabrainer.api.Goal
import me.cpele.fleabrainer.api.User
import me.cpele.fleabrainer.database.CustomDatabase
import me.cpele.fleabrainer.database.dao.DatapointDao
import me.cpele.fleabrainer.database.dao.GoalTimingDao
import me.cpele.fleabrainer.database.dao.StatusChangeDao
import me.cpele.fleabrainer.domain.*
import me.cpele.fleabrainer.ui.CustomApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase = Room
        .databaseBuilder(context, CustomDatabase::class.java, context.packageName)
        .addMigrations(MIGRATION_1_TO_2, MIGRATION_2_TO_3)
        .build()

    companion object {
        private val MIGRATION_1_TO_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE StatusChange ADD COLUMN message TEXT")
            }
        }
        private val MIGRATION_2_TO_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Nothing changed
            }
        }
    }

    private val goalTimingDao: GoalTimingDao = database.goalTimingDao()
    private val statusChangeDao: StatusChangeDao = database.statusDao()
    private val datapointDao: DatapointDao = database.datapointDao()

    val latestStatus: LiveData<StatusChange>
        get() {
            val distinctLiveData = MediatorLiveData<StatusChange>()
            distinctLiveData.addSource(
                statusChangeDao.findLatestStatus(),
                object : Observer<StatusChange> {
                    var previousValue: StatusChange? = null
                    override fun onChanged(value: StatusChange?) {
                        val isChanging = value?.status != previousValue?.status
                        if (isChanging) {
                            previousValue = value
                            distinctLiveData.value = value
                        }
                    }
                })
            return distinctLiveData
        }

    val goalTimings: LiveData<List<GoalTiming>> get() = goalTimingDao.findAll()

    private fun insertOrUpdateGoalTimings(
        user: String,
        list: List<Goal>,
        callback: () -> Unit = {}
    ) =
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

    private fun insertStatusChange(status: StatusChange, callback: () -> Unit = {}) {
        executor.execute {
            statusChangeDao.insert(status)
            callback()
        }
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
                    callback
                )
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                if (response?.isSuccessful == true) {
                    response.body()?.apply {
                        postQueuedDatapoints(accessToken, username) {
                            fetchGoals(accessToken, username, callback)
                        }
                    }
                } else {
                    val errorMsg = "Error loading user: status ${response?.code()}"
                    val errorStatus = Status.authError(errorMsg)
                    insertStatusChange(StatusChange(status = errorStatus), callback)
                }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String, callback: () -> Unit = {}) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
                insertStatusChange(
                    StatusChange(status = Status.failure("Error loading goals", t)),
                    callback
                )
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.apply {
                    insertOrUpdateGoalTimings(user, this) {
                        insertStatusChange(
                            StatusChange(
                                status = Status.SUCCESS,
                                message = "Goals fetched successfully"
                            ), callback
                        )
                    }
                }
            }
        })
    }

    fun persist(goalTiming: GoalTiming) {
        updateGoalTiming(goalTiming)
    }

    fun submit(goalTiming: GoalTiming, accessToken: String) {

        val userName = goalTiming.user
        val goalSlug = goalTiming.goal.slug
        val datapointValue = goalTiming.stopwatch.elapsedDecimalMinutes
        val comment = "via Fleabrainer at ${Date()}"

        goalTiming.stopwatch.clear()
        persist(goalTiming)

        postDatapoint(
            userName = userName,
            goalSlug = goalSlug,
            datapointValue = datapointValue,
            comment = comment,
            accessToken = accessToken
        )
    }

    private fun postDatapoint(
        datapointId: String? = null,
        userName: String,
        goalSlug: String,
        datapointValue: Float,
        comment: String,
        accessToken: String,
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
                        insertStatusChange(
                            StatusChange(
                                status = Status.FAILURE,
                                message =
                                "Submission failed: datapoint stored locally until next sync"
                            )
                        )
                    }
                    datapointId?.let(this@BeeRepository::asyncDeleteDatapointById)
                    enqueueDatapoint(userName, goalSlug, datapointValue, comment)
                }

                override fun onResponse(call: Call<Datapoint>?, response: Response<Datapoint>?) {
                    if (indicateStatusChange) {
                        insertStatusChange(
                            StatusChange(
                                status = Status.SUCCESS,
                                message = "Datapoint submitted successfully"
                            )
                        )
                    }
                    datapointId?.let(this@BeeRepository::asyncDeleteDatapointById)
                    asyncFindDatapointsBySlug(goalSlug, userName, accessToken)
                    executor.execute {
                        Thread.sleep(5000)
                        fetchGoals(accessToken, userName)
                    }
                }
            })
    }

    private fun asyncDeleteDatapointById(id: String) {
        executor.execute { datapointDao.deleteById(id) }
    }

    private fun enqueueDatapoint(
        userName: String,
        goalSlug: String,
        datapointValue: Float,
        comment: String
    ) {
        executor.execute {
            val datapoint = DatapointBo(
                id = UUID.randomUUID().toString(),
                goalSlug = goalSlug,
                userName = userName,
                datapointValue = datapointValue,
                comment = comment,
                pending = true,
                updatedAt = Date()
            )
            datapointDao.insertOne(datapoint)
        }
    }

    private fun postQueuedDatapoints(accessToken: String, userName: String, callback: () -> Unit) {
        executor.execute {
            val pendingDatapoints = datapointDao.findPendingByUser(userName)
            Log.d(javaClass.simpleName, "Found pending datapoints: $pendingDatapoints")
            pendingDatapoints.forEach { datapoint ->
                datapoint.pending = false
                datapointDao.insertOne(datapoint)
                val goalTiming = goalTimingDao.findOneBySlug(datapoint.goalSlug)
                goalTiming?.let {
                    postDatapoint(
                        datapoint.id,
                        userName,
                        datapoint.goalSlug,
                        datapoint.datapointValue,
                        datapoint.comment,
                        accessToken,
                        indicateStatusChange = false
                    )
                }
            }
            callback()
        }
    }

    fun asyncFindGoalTimingBySlug(slug: String): LiveData<GoalTiming> {
        return goalTimingDao.asyncFindOneBySlug(slug)
    }

    fun forceRefreshGoalTimingBySlug(slug: String) {
        executor.execute {
            goalTimingDao
                .findOneBySlug(slug)
                ?.let { goalTimingDao.insertOne(it) }
        }
    }

    fun asyncCancelStopwatch(slug: String) {
        executor.execute {
            val goalTiming = goalTimingDao.findOneBySlug(slug)
            goalTiming?.stopwatch?.apply {
                clear()
                persist(goalTiming)
            }
        }
    }

    fun asyncSubmit(slug: String, accessToken: String) {
        executor.execute {
            val goalTiming = goalTimingDao.findOneBySlug(slug)
            goalTiming?.apply {
                submit(this, accessToken)
            }
        }
    }

    fun asyncFindDatapointsBySlug(
        slug: String,
        userName: String,
        accessToken: String
    ): LiveData<List<DatapointBo>> {

        insertStatusChange(StatusChange(status = Status.LOADING))

        CustomApp.instance.api
            .getDataPoints(userName, slug, accessToken)
            .enqueue(object : Callback<List<Datapoint>> {
                override fun onFailure(call: Call<List<Datapoint>>?, t: Throwable?) {
                    insertStatusChange(
                        StatusChange(
                            status = Status.FAILURE,
                            message = "Error getting datapoints"
                        )
                    )
                    Log.w(BeeRepository::class.java.simpleName, t)
                }

                override fun onResponse(
                    call: Call<List<Datapoint>>?,
                    response: Response<List<Datapoint>>?
                ) {
                    val body = response?.body()
                    if (body != null) {
                        insertDatapoints(body, userName, slug)
                        insertStatusChange(StatusChange(status = Status.SUCCESS))
                    } else {
                        insertStatusChange(
                            StatusChange(
                                status = Status.FAILURE,
                                message = "Response received but body is null"
                            )
                        )
                    }
                }
            })

        return datapointDao.findBySlug(userName, slug)
    }

    private fun insertDatapoints(body: List<Datapoint>, userName: String, slug: String) {
        executor.execute {
            datapointDao.insert(body.map {
                DatapointBo(
                    id = it.id,
                    goalSlug = slug,
                    userName = userName,
                    datapointValue = it.value.toFloat(),
                    comment = it.comment,
                    pending = false,
                    updatedAt = it.updated_at
                )
            })
        }
    }

    fun toggleThenStopOthers(slugToToggle: String) = executor.execute {
        goalTimingDao.findAllSync()
            .forEach {
                when (it.goal.slug) {
                    slugToToggle -> it.stopwatch.toggle()
                    else -> it.stopwatch.stop()
                }
                goalTimingDao.insertOne(it)
            }
    }
}
