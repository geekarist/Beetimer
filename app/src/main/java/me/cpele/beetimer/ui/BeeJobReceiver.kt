package me.cpele.beetimer.ui

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.concurrent.TimeUnit

private const val ACTION_START_BEE_JOB = "me.cpele.beetimer.ACTION_START_BEE_JOB"

class BeeJobReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action in listOf(Intent.ACTION_BOOT_COMPLETED, ACTION_START_BEE_JOB)) {
            context?.apply {
                val jobScheduler: JobScheduler =
                        this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val componentName = ComponentName(this, BeeJobService::class.java)
                val jobInfo = JobInfo.Builder(0, componentName)
                        .setPeriodic(TimeUnit.HOURS.toMillis(1))
                        .setMinimumLatency(0)
                        .build()
                jobScheduler.schedule(jobInfo)
                Log.d(BeeJobReceiver::class.java.simpleName, "Job scheduled")
            }
        }
    }

    class CustomIntent: Intent(ACTION_START_BEE_JOB)
}