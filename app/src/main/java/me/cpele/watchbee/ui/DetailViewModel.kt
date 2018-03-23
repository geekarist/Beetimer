package me.cpele.watchbee.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import me.cpele.watchbee.repository.BeeRepository

class DetailViewModel(
        private val beeRepository: BeeRepository,
        private val slug: String
) : ViewModel() {

    val goalTiming get() = beeRepository.asyncFindGoalTimingBySlug(slug)

    class Factory(
            private val beeRepository: BeeRepository,
            private val slug: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return modelClass.cast(DetailViewModel(beeRepository, slug))
        }
    }
}
