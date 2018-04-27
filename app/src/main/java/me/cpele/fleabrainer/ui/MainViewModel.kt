package me.cpele.fleabrainer.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.preference.PreferenceManager
import me.cpele.fleabrainer.domain.GoalTiming
import me.cpele.fleabrainer.domain.StatusChange
import me.cpele.fleabrainer.repository.BeeRepository

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

    fun submit(context: Context, goalTiming: GoalTiming) {
        repository.submit(
                goalTiming,
                PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString(SignInActivity.PREF_ACCESS_TOKEN, null)
        )
    }
}

