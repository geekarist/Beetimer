package me.cpele.watchbee.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import me.cpele.watchbee.R
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.StatusChange
import me.cpele.watchbee.repository.BeeRepository

class DetailViewModel(
        private val beeRepository: BeeRepository,
        private val slug: String
) : ViewModel() {

    val goalTiming: LiveData<GoalTiming> = beeRepository.asyncFindGoalTimingBySlug(slug)
    val status: LiveData<StatusChange> = beeRepository.latestStatus

    fun startStopDrawable(context: Context): LiveData<Drawable> {
        val drawableData = MediatorLiveData<Drawable>()
        drawableData.addSource(goalTiming, {
            drawableData.value =
                    when (it?.stopwatch?.running) {
                        false -> context.getDrawable(R.drawable.ic_play_arrow_black_24dp)
                        else -> context.getDrawable(R.drawable.ic_pause_black_24dp)
                    }
        })
        return drawableData
    }

    fun forceRefresh() {
        beeRepository.forceRefreshGoalTimingBySlug(slug)
    }

    fun onToggle() {
        beeRepository.asyncToggleStopwatch(slug)
    }

    fun onCancel() {
        beeRepository.asyncCancelStopwatch(slug)
    }

    fun onSubmit(context: Context) {
        beeRepository.asyncSubmit(
                slug,
                PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString(SignInActivity.PREF_ACCESS_TOKEN, null)
        )
    }

    class Factory(
            private val beeRepository: BeeRepository,
            private val slug: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.cast(DetailViewModel(beeRepository, slug))
        }
    }
}
