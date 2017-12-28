package me.cpele.beetimer.ui

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import java.util.concurrent.TimeUnit

class BeeJobReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_START_BEE_JOB = "me.cpele.beetimer.ACTION_START_BEE_JOB"
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        if (intent?.action in listOf(Intent.ACTION_BOOT_COMPLETED, ACTION_START_BEE_JOB)) {
            context?.apply {
                val jobScheduler: JobScheduler =
                        this.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
                val componentName = ComponentName(this, BeeJobService::class.java)
                val jobInfo = JobInfo.Builder(0, componentName)
                        .setPeriodic(TimeUnit.HOURS.toMillis(1))
                        .setExtras(toPersistableBundle(intent?.extras))
                        .setMinimumLatency(0)
                        .build()
                jobScheduler.schedule(jobInfo)
                Log.d(BeeJobReceiver::class.java.simpleName, "Job scheduled")
            }
        }
    }

    private fun toPersistableBundle(extras: Bundle?): PersistableBundle? {
        val persistableBundle = PersistableBundle()
        val extraAuthToken = extras?.getString(CustomIntent.EXTRA_AUTH_TOKEN)
                ?: throw IllegalStateException("Intent should have EXTRA_AUTH_TOKEN")
        persistableBundle.putString(CustomIntent.EXTRA_AUTH_TOKEN, extraAuthToken)
        return persistableBundle
    }

    class CustomIntent(extraAuthToken: String) : Intent(ACTION_START_BEE_JOB) {

        companion object {
            const val EXTRA_AUTH_TOKEN = "me.cpele.beetimer.EXTRA_AUTH_TOKEN"
        }

        init {
            putExtra(EXTRA_AUTH_TOKEN, extraAuthToken)
        }
    }
}