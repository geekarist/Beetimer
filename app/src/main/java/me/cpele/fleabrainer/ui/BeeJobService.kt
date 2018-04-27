package me.cpele.fleabrainer.ui

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.PersistableBundle
import android.util.Log

class BeeJobService : JobService() {

    companion object {

        private val EXTRA_AUTH_TOKEN = "EXTRA_AUTH_TOKEN"

        fun newBundle(extraAuthToken: String): PersistableBundle {
            val persistableBundle = PersistableBundle()
            persistableBundle.putString(EXTRA_AUTH_TOKEN, extraAuthToken)
            return persistableBundle
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(javaClass.simpleName, "Stop job")
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(javaClass.simpleName, "Start job")
        val extraName = EXTRA_AUTH_TOKEN
        val authToken: String = params?.extras?.getString(extraName)
                ?: throw IllegalStateException("Intent should have EXTRA_AUTH_TOKEN")
        CustomApp.instance.beeRepository.fetch(authToken) { jobFinished(params, false) }
        return true
    }
}