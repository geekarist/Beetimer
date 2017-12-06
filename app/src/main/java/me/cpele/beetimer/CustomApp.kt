package me.cpele.beetimer

import android.app.Application
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CustomApp : Application() {

    companion object {
        lateinit var instance: CustomApp private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    val api: BeeminderApi by lazy {
        val retrofit = Retrofit.Builder()
                .baseUrl("https://www.beeminder.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        retrofit.create(BeeminderApi::class.java)
    }
}