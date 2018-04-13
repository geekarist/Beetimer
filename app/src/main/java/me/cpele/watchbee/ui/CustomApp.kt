package me.cpele.watchbee.ui

import android.app.Application
import com.google.gson.GsonBuilder
import me.cpele.watchbee.BuildConfig
import me.cpele.watchbee.api.EpochTypeAdapter
import me.cpele.watchbee.repository.BeeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class CustomApp : Application() {

    private lateinit var executors: me.cpele.watchbee.AppExecutors

    companion object {
        lateinit var instance: CustomApp private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        executors = me.cpele.watchbee.AppExecutors()
    }

    val api: me.cpele.watchbee.api.BeeminderApi by lazy {
        val okHttpClient = OkHttpClient.Builder()
                .let {
                    if (BuildConfig.DEBUG) {
                        val loggingInterceptor = HttpLoggingInterceptor()
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                        it.addInterceptor(loggingInterceptor)
                    } else it
                }.build()
        val gson = GsonBuilder().registerTypeAdapter(Date::class.java, EpochTypeAdapter()).create()
        val converterFactory = GsonConverterFactory.create(gson)
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.beeminder.com")
                .addConverterFactory(converterFactory)
                .client(okHttpClient)
                .build()
        retrofit.create(me.cpele.watchbee.api.BeeminderApi::class.java)
    }

    val beeRepository: BeeRepository by lazy {
        BeeRepository(this, executors.disk)
    }
}

