package me.cpele.beetimer.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import me.cpele.beetimer.domain.GoalTiming
import me.cpele.beetimer.domain.StatusChange
import me.cpele.beetimer.repository.BeeRepository

class MainViewModel(
        private val repository: BeeRepository,
        private val authToken: String?
) : ViewModel() {

    val status: LiveData<StatusChange> = repository.latestStatus
    val goalTimings: LiveData<List<GoalTiming>> = repository.goalTimings

    fun refresh() = repository.fetch(authToken)

    class Factory(
            private val repository: BeeRepository,
            private val authToken: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                modelClass.cast(MainViewModel(repository, authToken))
    }

    fun persist(goalTiming: GoalTiming) = repository.persist(goalTiming)

}

