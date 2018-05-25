package me.cpele.fleabrainer.ui

import android.app.Application
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.experimental.CoroutineCallAdapterFactory
import me.cpele.fleabrainer.BuildConfig
import me.cpele.fleabrainer.api.EpochTypeAdapter
import me.cpele.fleabrainer.repository.BeeRepository
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.Executors

class CustomApp : Application() {

    private lateinit var executors: me.cpele.fleabrainer.AppExecutors

    companion object {
        lateinit var instance: CustomApp private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        executors = me.cpele.fleabrainer.AppExecutors()
    }

    val api: me.cpele.fleabrainer.api.BeeminderApi by lazy {
        val okHttpClient = OkHttpClient.Builder()
                .dispatcher(Dispatcher(Executors.newSingleThreadExecutor()))
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
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(okHttpClient)
                .build()
        retrofit.create(me.cpele.fleabrainer.api.BeeminderApi::class.java)
    }

    val beeRepository: BeeRepository by lazy {
        BeeRepository(this, executors.disk)
    }
}

