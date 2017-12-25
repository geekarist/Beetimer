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
        return false
    }
}