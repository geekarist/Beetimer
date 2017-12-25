package me.cpele.beetimer.ui

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.util.Log
import me.cpele.beetimer.AppExecutors
import me.cpele.beetimer.BuildConfig
import me.cpele.beetimer.api.BeeminderApi
import me.cpele.beetimer.repository.BeeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class CustomApp : Application() {

    companion object {
        lateinit var instance: CustomApp private set
    }

    val executors = AppExecutors()

    override fun onCreate() {
        super.onCreate()
        instance = this

        val jobScheduler: JobScheduler =
                getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val componentName = ComponentName(this, BeeJobService::class.java)
        val jobInfo = JobInfo.Builder(0, componentName)
                .setPeriodic(TimeUnit.HOURS.toMillis(1))
                .setMinimumLatency(0)
                .build()
        jobScheduler.schedule(jobInfo)
        Log.d(javaClass.simpleName, "Job scheduled")
    }

    val api: BeeminderApi by lazy {
        val okHttpClient = OkHttpClient.Builder()
                .let {
                    if (BuildConfig.DEBUG) {
                        val loggingInterceptor = HttpLoggingInterceptor()
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(loggingInterceptor)
                    } else it
                }.build()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.beeminder.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        retrofit.create(BeeminderApi::class.java)
    }

    val beeRepository: BeeRepository by lazy {
        BeeRepository(this, executors.disk)
    }
}

