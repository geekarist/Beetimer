package me.cpele.beetimer.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User
import me.cpele.beetimer.database.*
import me.cpele.beetimer.domain.Status
import me.cpele.beetimer.ui.CustomApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase = Room
            .databaseBuilder(context, CustomDatabase::class.java, context.packageName)
            .build()

    private val userDao: UserDao = database.userDao()
    private val goalDao: GoalDao = database.goalDao()
    private val statusDao: StatusDao = database.statusDao()

    val latestStatus: LiveData<StatusContainer> = statusDao.findLatestStatus()
    val goals: LiveData<List<Goal>> = goalDao.findAllGoals()

    private fun insertOrUpdateUser(user: User) = executor.execute { userDao.insertOrUpdate(user) }
    private fun insertOrUpdateGoals(list: List<Goal>) = executor.execute { goalDao.insertOrUpdate(list) }
    private fun updateLatestStatus(status: StatusContainer) =
            executor.execute { statusDao.updateLatestStatus(status) }

    fun fetch(authToken: String?) = authToken?.apply { fetchUser(this) }

    private fun fetchUser(accessToken: String) {

        updateLatestStatus(StatusContainer(Status.LOADING))

        CustomApp.instance.api.getUser(accessToken).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                updateLatestStatus(StatusContainer(Status.failure("Error loading user", t)))
            }

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.apply {
                    insertOrUpdateUser(this)
                    fetchGoals(accessToken, username)
                }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) {
                updateLatestStatus(StatusContainer(Status.failure("Error loading goals", t)))
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.apply {
                    insertOrUpdateGoals(this)
                    updateLatestStatus(StatusContainer(Status.SUCCESS))
                }
            }
        })
    }
}

