package me.cpele.watchbee.domain

import java.util.concurrent.TimeUnit

class Stopwatch(
        var startTime: Long = 0,
        var stopTime: Long = 0,
        var running: Boolean = false,
        var elapsedPreviously: Long = 0
) {
    val elapsedDecimalMinutes: Float get() = elapsedMillis / (1000f * 60f * 60f)

    private val elapsedMillis: Long
        get() {
            return elapsedPreviously +
                    if (running) System.currentTimeMillis() - startTime
                    else stopTime - startTime
        }

    fun clear() {
        startTime = 0
        stopTime = 0
        running = false
        elapsedPreviously = 0
    }

    fun toggle() {
        if (!running) {
            elapsedPreviously += stopTime - startTime
            startTime = System.currentTimeMillis()
            running = true
        } else {
            stopTime = System.currentTimeMillis()
            running = false
        }
    }

    fun format(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis
                - TimeUnit.MINUTES.toMillis(minutes)
                - TimeUnit.HOURS.toMillis(hours))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}