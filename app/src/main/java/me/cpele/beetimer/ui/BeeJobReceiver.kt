package me.cpele.beetimer.ui

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.PersistableBundle
import android.preference.PreferenceManager
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

                val persistableBundle = newBeeJobServiceBundle(context, intent)

                val jobInfo = JobInfo.Builder(0, componentName)
                        .setPeriodic(TimeUnit.HOURS.toMillis(1))
                        .setExtras(persistableBundle)
                        .setPersisted(true)
                        .build()
                jobScheduler.schedule(jobInfo)
                Log.d(BeeJobReceiver::class.java.simpleName, "Job scheduled")
            }
        }
    }

    // TODO move to BeeJobService somehow
    private fun newBeeJobServiceBundle(context: Context?, intent: Intent?): PersistableBundle {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val persistableBundle = PersistableBundle()
        val extraAuthToken = obtainAuthToken(intent, preferences)
        persistableBundle.putString(CustomIntent.EXTRA_AUTH_TOKEN, extraAuthToken)
        return persistableBundle
    }

    private fun obtainAuthToken(intent: Intent?, preferences: SharedPreferences) =
            intent?.extras?.getString(CustomIntent.EXTRA_AUTH_TOKEN)
                    ?: preferences.getString(SignInActivity.PREF_ACCESS_TOKEN, null)
                    ?: throw IllegalStateException("Auth token should be in intent or shared preferences")

    class CustomIntent(extraAuthToken: String) : Intent(ACTION_START_BEE_JOB) {

        companion object {
            const val EXTRA_AUTH_TOKEN = "me.cpele.beetimer.EXTRA_AUTH_TOKEN"
        }

        init {
            putExtra(EXTRA_AUTH_TOKEN, extraAuthToken)
        }
    }
}