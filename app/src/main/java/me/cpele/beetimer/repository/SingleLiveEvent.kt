package me.cpele.beetimer.repository

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val pending: AtomicBoolean = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<T>) {
        super.observe(owner, Observer<T> {
            if (pending.compareAndSet(true, false)) observer.onChanged(it)
        })
    }

    override fun setValue(value: T) {
        pending.set(true)
        super.setValue(value)
    }
}