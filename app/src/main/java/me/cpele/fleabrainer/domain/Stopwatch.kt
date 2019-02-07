package me.cpele.fleabrainer.domain

import android.support.annotation.ColorRes
import me.cpele.fleabrainer.R
import java.util.concurrent.TimeUnit

class Stopwatch(
    var startTime: Long = 0,
    var stopTime: Long = 0,
    var running: Boolean = false,
    var elapsedPreviously: Long = 0
) : Comparable<Stopwatch> {

    override fun compareTo(other: Stopwatch): Int {
        return if (running != other.running) {
            if (running) -1 else 1
        } else {
            (other.elapsedMillis - elapsedMillis).toInt()
        }
    }

    val elapsedDecimalMinutes: Float get() = elapsedMillis / (1000f * 60f * 60f)

    private val elapsedMillis: Long
        get() {
            return elapsedPreviously +
                    if (running) System.currentTimeMillis() - startTime
                    else stopTime - startTime
        }

    val hours: Int get() = TimeUnit.MILLISECONDS.toHours(elapsedMillis).toInt()

    val minutes: Int
        get() {
            return TimeUnit
                .MILLISECONDS.toMinutes(elapsedMillis - TimeUnit.HOURS.toMillis(hours.toLong()))
                .toInt()
        }

    fun clear() {
        startTime = 0
        stopTime = 0
        running = false
        elapsedPreviously = 0
    }

    fun toggle() {
        if (!running) {
            start()
        } else {
            stop()
        }
    }

    fun stop() {
        if (!running) return
        stopTime = System.currentTimeMillis()
        running = false
    }

    fun start() {
        if (running) return
        elapsedPreviously += stopTime - startTime
        startTime = System.currentTimeMillis()
        running = true
    }

    fun format(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
        val minutes =
            TimeUnit.MILLISECONDS.toMinutes(elapsedMillis - TimeUnit.HOURS.toMillis(hours))
        val seconds = TimeUnit.MILLISECONDS.toSeconds(
            elapsedMillis
                    - TimeUnit.MINUTES.toMillis(minutes)
                    - TimeUnit.HOURS.toMillis(hours)
        )
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    @ColorRes
    fun color(): Int {
        return when (running) {
            false -> when (elapsedMillis > 0) {
                false -> R.color.stopwatch_stopped
                true -> R.color.stopwatch_paused
            }
            true -> R.color.stopwatch_running
        }
    }

    fun set(hours: Int, minutes: Int) {
        clear()
        elapsedPreviously =
            TimeUnit.HOURS.toMillis(hours.toLong()) +
                    TimeUnit.MINUTES.toMillis(minutes.toLong())
    }
}