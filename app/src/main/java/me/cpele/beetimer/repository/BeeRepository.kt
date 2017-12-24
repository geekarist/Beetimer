package me.cpele.beetimer.repository

import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.Room
import android.content.Context
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.api.User
import me.cpele.beetimer.database.CustomDatabase
import me.cpele.beetimer.database.GoalDao
import me.cpele.beetimer.database.UserDao
import me.cpele.beetimer.ui.CustomApp
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

class BeeRepository(context: Context, private val executor: Executor) {

    private val database: CustomDatabase = Room
            .databaseBuilder(context, CustomDatabase::class.java, context.packageName)
            .build()

    private val userDao: UserDao
    private val goalDao: GoalDao

    val loadingInProgressEvent = MutableLiveData<LoadingInProgressEvent>()
    val loadingErrorEvent = MutableLiveData<LoadingErrorEvent>()
    val loadingSuccessEvent = MutableLiveData<LoadingSuccessEvent>()

    init {
        userDao = database.userDao()
        goalDao = database.goalDao()
    }

    fun insertOrUpdateUser(user: User) = executor.execute { userDao.insertOrUpdate(user) }
    fun insertOrUpdateGoals(list: List<Goal>) = executor.execute { goalDao.insertOrUpdate(list) }
    fun fetch(authToken: String?) = authToken?.apply { fetchUser(this) }

    private fun fetchUser(accessToken: String) {

        loadingInProgressEvent.value = LoadingInProgressEvent()

        CustomApp.instance.api.getUser(accessToken).enqueue(object : Callback<User> {
            override fun onFailure(call: Call<User>?, t: Throwable?) {
                loadingErrorEvent.value = LoadingErrorEvent("Error loading user", t)
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
                loadingErrorEvent.value = LoadingErrorEvent("Error loading goals", t)
            }

            override fun onResponse(call: Call<List<Goal>>?, response: Response<List<Goal>>?) {
                response?.body()?.apply {
                    insertOrUpdateGoals(this)
                    loadingSuccessEvent.setValue(LoadingSuccessEvent(this))
                }
            }
        })
    }
}

