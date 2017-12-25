package me.cpele.beetimer.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.database.StatusContainer
import me.cpele.beetimer.repository.BeeRepository

class MainViewModel(
        private val repository: BeeRepository,
        private val authToken: String?
) : ViewModel() {

    val status: LiveData<StatusContainer> = repository.latestStatus
    val goals: LiveData<List<Goal>> = repository.goals

    fun refresh() = repository.fetch(authToken)

    class Factory(
            private val repository: BeeRepository,
            private val authToken: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                modelClass.cast(MainViewModel(repository, authToken))
    }

}

