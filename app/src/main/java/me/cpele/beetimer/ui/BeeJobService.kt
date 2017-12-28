package me.cpele.beetimer.ui

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class BeeJobService : JobService() {

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(javaClass.simpleName, "Stop job")
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(javaClass.simpleName, "Start job")
        val extraName = BeeJobReceiver.CustomIntent.EXTRA_AUTH_TOKEN
        val authToken: String = params?.extras?.getString(extraName)
                ?: throw IllegalStateException("Intent should have EXTRA_AUTH_TOKEN")
        CustomApp.instance.beeRepository.fetch(authToken) { jobFinished(params, false) }
        return true
    }
}