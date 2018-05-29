package me.cpele.fleabrainer.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.util.Log
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import me.cpele.fleabrainer.api.Datapoint
import me.cpele.fleabrainer.api.Goal
import me.cpele.fleabrainer.database.CustomDatabase
import me.cpele.fleabrainer.database.dao.DatapointDao
import me.cpele.fleabrainer.database.dao.GoalTimingDao
import me.cpele.fleabrainer.database.dao.StatusChangeDao
import me.cpele.fleabrainer.domain.*
import me.cpele.fleabrainer.ui.CustomApp
import java.io.IOException
import java.util.*

// TODO: Repair toasts on submit
class BeeRepository(context: Context) {

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
            distinctLiveData.addSource(statusChangeDao.findLatestStatus(), object : Observer<StatusChange> {
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
            list: List<Goal>
    ) = async {
        list.map { goal ->
            val goalTiming =
                    goalTimingDao.findOneBySlug(goal.slug)
                            ?: GoalTiming(user = user, goal = goal, stopwatch = Stopwatch())
            goalTiming.goal = goal
            goalTiming
        }.let { goalTimings ->
            goalTimingDao.insert(goalTimings)
        }
    }

    private fun insertStatusChange(status: StatusChange) = async {
        statusChangeDao.insert(status)
    }

    private fun updateGoalTiming(goalTiming: GoalTiming) = launch {
        goalTimingDao.insertOne(goalTiming)
    }

    fun fetch(authToken: String?) {
        authToken?.apply { fetchUser(this) }
    }

    private fun fetchUser(accessToken: String) = launch {
        insertStatusChange(StatusChange(status = Status.LOADING))

        try {
            val response = CustomApp.instance.api.getUser(accessToken).await()
            if (response?.isSuccessful == true) {
                response.body()?.apply {
                    postQueuedDatapoints(accessToken, username).await()
                    fetchGoals(accessToken, username)
                }
            } else {
                val errorMsg = "Error loading user: status ${response?.code()}"
                val errorStatus = Status.authError(errorMsg)
                insertStatusChange(StatusChange(status = errorStatus))
            }
        } catch (e: IOException) {
            insertStatusChange(StatusChange(status = Status.failure(
                    "Error loading user", e))
            )
        }
    }

    private fun fetchGoals(accessToken: String, user: String) = launch {
        try {
            val response = CustomApp.instance.api.getGoals(user, accessToken).await()
            response?.body()?.apply {
                insertOrUpdateGoalTimings(user, this).await()
                insertStatusChange(StatusChange(status = Status.SUCCESS))
            }
        } catch (e: IOException) {
            insertStatusChange(StatusChange(status = Status.failure("Error loading goals", e)))
        }
    }

    fun persist(goalTiming: GoalTiming) {
        updateGoalTiming(goalTiming)
    }

    private val _submissionResult: MutableLiveData<StatusChange> = MutableLiveData()
    val submissionResult: LiveData<StatusChange> = _submissionResult

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
    ) = launch {
        if (indicateStatusChange) insertStatusChange(StatusChange(status = Status.LOADING))

        try {
            CustomApp.instance.api.postDatapoint(
                    userName,
                    goalSlug,
                    datapointValue,
                    comment,
                    accessToken
            ).await()

            if (indicateStatusChange) {
                _submissionResult.postValue(StatusChange(
                        status = Status.SUCCESS,
                        message = "Datapoint submitted successfully"
                ))
            }

            datapointId?.let(this@BeeRepository::asyncDeleteDatapointById)
            asyncFindDatapointsBySlug(goalSlug, userName, accessToken)

            delay(5000)
            fetchGoals(accessToken, userName)

            if (indicateStatusChange) insertStatusChange(StatusChange(status = Status.SUCCESS))

        } catch (e: IOException) {
            val tag = BeeRepository::class.java.simpleName
            Log.e(tag, "Error posting goal timing", e)
            if (indicateStatusChange) {
                _submissionResult.postValue(StatusChange(
                        status = Status.FAILURE,
                        message = "Submission failed: datapoint stored locally until next sync"
                ))
                insertStatusChange(StatusChange(status = Status.FAILURE))
            }
            datapointId?.let(this@BeeRepository::asyncDeleteDatapointById)
            enqueueDatapoint(userName, goalSlug, datapointValue, comment)
        }
    }

    private fun asyncDeleteDatapointById(id: String) = launch {
        datapointDao.deleteById(id)
    }

    private fun enqueueDatapoint(
            userName: String,
            goalSlug: String,
            datapointValue: Float,
            comment: String
    ) = launch {
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

    private fun postQueuedDatapoints(accessToken: String, userName: String) = async {
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
    }

    fun asyncFindGoalTimingBySlug(slug: String): LiveData<GoalTiming> {
        return goalTimingDao.asyncFindOneBySlug(slug)
    }

    fun forceRefreshGoalTimingBySlug(slug: String) = launch {
        goalTimingDao
                .findOneBySlug(slug)
                ?.let { goalTimingDao.insertOne(it) }
    }

    fun asyncToggleStopwatch(slug: String) = launch {
        val goalTiming = goalTimingDao.findOneBySlug(slug)
        goalTiming?.stopwatch?.apply {
            toggle()
            persist(goalTiming)
        }
    }

    fun asyncCancelStopwatch(slug: String) = launch {
        val goalTiming = goalTimingDao.findOneBySlug(slug)
        goalTiming?.stopwatch?.apply {
            clear()
            persist(goalTiming)
        }
    }

    fun asyncSubmit(slug: String, accessToken: String) = launch {
        val goalTiming = goalTimingDao.findOneBySlug(slug)
        goalTiming?.apply {
            submit(this, accessToken)
        }
    }

    fun asyncFindDatapointsBySlug(
            slug: String,
            userName: String,
            accessToken: String
    ): LiveData<List<DatapointBo>> {

        insertStatusChange(StatusChange(status = Status.LOADING))

        launch {

            try {
                val response = CustomApp.instance.api.getDataPoints(userName, slug, accessToken).await()

                val body = response?.body()
                if (body != null) {
                    insertDatapoints(body, userName, slug)
                    insertStatusChange(StatusChange(status = Status.SUCCESS))
                } else {
                    insertStatusChange(StatusChange(
                            status = Status.FAILURE,
                            message = "Response received but body is null"
                    ))
                }

            } catch (e: IOException) {
                insertStatusChange(StatusChange(
                        status = Status.FAILURE,
                        message = "Error getting datapoints"
                ))
                Log.w(BeeRepository::class.java.simpleName, e)
            }
        }

        return datapointDao.findBySlug(userName, slug)
    }

    private fun insertDatapoints(
            datapoints: List<Datapoint>,
            userName: String,
            slug: String
    ): Deferred<Unit> = async {
        datapointDao.insert(datapoints.map {
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
