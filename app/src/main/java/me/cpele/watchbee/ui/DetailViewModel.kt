package me.cpele.watchbee.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import me.cpele.watchbee.api.Goal
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.Stopwatch
import me.cpele.watchbee.repository.BeeRepository

class DetailViewModel(
        private val beeRepository: BeeRepository,
        private val slug: String
) : ViewModel() {

    val goalTiming: MutableLiveData<GoalTiming>
        get() {
            return MutableLiveData()
        }

    fun fetch() {
        goalTiming.postValue(
                GoalTiming(
                        user = "user",
                        goal = Goal(
                                slug = "goal",
                                title = "title",
                                rate = "rate",
                                delta_text = "delta_text",
                                lane = 0,
                                limsum = "linsum",
                                losedate = 0,
                                runits = "runits",
                                yaw = 0
                        ),
                        stopwatch = Stopwatch()
                ))
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
