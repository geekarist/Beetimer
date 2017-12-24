package me.cpele.beetimer.ui

import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.repository.BeeRepository

private const val CHILD_LOADING = 0
private const val CHILD_GOALS = 1
private const val CHILD_ERROR = 2

class MainViewModel(
        private val repository: BeeRepository,
        private val authToken: String?
) : ViewModel() {

    val status: MediatorLiveData<Status> = MediatorLiveData<Status>()
    val goals: MediatorLiveData<List<Goal>> = MediatorLiveData<List<Goal>>()

    init {
        repository.apply {
            status.addSource(loadingSuccessEvent) { status.value = Status.SUCCESS }
            status.addSource(loadingErrorEvent) { status.value = Status.FAILURE }
            status.addSource(loadingInProgressEvent) { status.value = Status.LOADING }

            goals.addSource(loadingSuccessEvent) { goals.value = it?.goals ?: emptyList() }
            goals.addSource(loadingInProgressEvent) { goals.value = emptyList() }
            goals.addSource(loadingErrorEvent) { goals.value = emptyList() }
        }
    }

    fun refresh() = repository.fetch(authToken)

    class Factory(
            private val repository: BeeRepository,
            private val authToken: String?
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                modelClass.cast(MainViewModel(repository, authToken))
    }

    enum class Status(val displayedChild: Int) {
        SUCCESS(CHILD_GOALS),
        LOADING(CHILD_LOADING),
        FAILURE(CHILD_ERROR)
    }
}

