package me.cpele.beetimer.ui

import android.app.Application
import me.cpele.beetimer.AppExecutors
import me.cpele.beetimer.BuildConfig
import me.cpele.beetimer.api.BeeminderApi
import me.cpele.beetimer.repository.BeeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomApp : Application() {

    private lateinit var executors: AppExecutors

    companion object {
        lateinit var instance: CustomApp private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        executors = AppExecutors()
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

