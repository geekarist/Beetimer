package me.cpele.fleabrainer

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {
    val disk: Executor = Executors.newSingleThreadExecutor()
    val network: Executor = Executors.newFixedThreadPool(3)
    val main: Executor = me.cpele.fleabrainer.MainThreadExecutor()
}

private class MainThreadExecutor : Executor {
    val handler = Handler(Looper.getMainLooper())
    override fun execute(command: Runnable?) {
        handler.post(command)
    }
}