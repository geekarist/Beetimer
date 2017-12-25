package me.cpele.beetimer.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Room
import android.content.Context
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User
import me.cpele.beetimer.database.CustomDatabase
import me.cpele.beetimer.database.dao.GoalDao
import me.cpele.beetimer.database.dao.StatusChangeDao
import me.cpele.beetimer.database.dao.UserDao
import me.cpele.beetimer.domain.Status
import me.cpele.beetimer.domain.StatusChange
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
    private val statusChangeDao: StatusChangeDao = database.statusDao()

    val latestStatus: LiveData<StatusChange> = statusChangeDao.findLatestStatus()
    val goals: LiveData<List<Goal>> = goalDao.findAllGoals()

    private fun insertUser(user: User) = executor.execute { userDao.insert(user) }
    private fun insertGoals(list: List<Goal>) = executor.execute { goalDao.insert(list) }
    private fun insertStatusChange(status: StatusChange) =
            executor.execute { statusChangeDao.insert(status) }

    fun fetch(authToken: String?) = authToken?.apply { fetchUser(this) }

    private fun fetchUser(accessToken: String) {

        insertStatusChange(StatusChange(status = Status.LOADING))

        CustomApp.instance.api.getUser(accessToken).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) =
                insertStatusChange(StatusChange(status = Status.failure("Error loading user", t)))

            override fun onResponse(call: Call<User>?, response: Response<User>?) {
                response?.body()?.apply {
                    insertUser(this)
                    fetchGoals(accessToken, username)
                }
            }
        })
    }

    private fun fetchGoals(accessToken: String, user: String) {

        CustomApp.instance.api.getGoals(user, accessToken).enqueue(object : Callback<List<Goal>> {
            override fun onFailure(call: Call<List<Goal>>?, t: Throwable?) =
                insertStatusChange(StatusChange(status = Status.failure("Error loading goals", t)))

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.apply {
                    insertGoals(this)
                    insertStatusChange(StatusChange(status = Status.SUCCESS))
                }
            }
        })
    }
}

