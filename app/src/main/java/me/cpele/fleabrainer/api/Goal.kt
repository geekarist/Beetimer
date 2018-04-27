package me.cpele.fleabrainer.api

import android.support.annotation.ColorRes
import me.cpele.fleabrainer.R
import java.util.concurrent.TimeUnit

data class Goal(
        val slug: String,
        val title: String,
        val rate: String,
        val delta_text: String,
        val losedate: Long,
        val lane: Int,
        val yaw: Int,
        val runits: String,
        val limsum: String) {

    val color: Int
        @ColorRes
        get() {
            val laneTimesYaw = lane * yaw
            if (laneTimesYaw > 1) return R.color.green
            else if (laneTimesYaw == 1) return R.color.blue
            else if (laneTimesYaw == -1) return R.color.orange
            else if (laneTimesYaw <= -2) return R.color.red
            return android.R.color.white
        }

    val derailDays: Long
        get() {
            return TimeUnit.SECONDS.toDays(losedate) -
                    TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        }

    val derailHours: Long
        get() {
            return TimeUnit.SECONDS.toHours(losedate) -
                    TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis()) -
                    TimeUnit.DAYS.toHours(derailDays)
        }

    val derailMin: Long
        get() {
            return TimeUnit.SECONDS.toMinutes(losedate) -
                    TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) -
                    TimeUnit.DAYS.toMinutes(derailDays) -
                    TimeUnit.HOURS.toMinutes(derailHours)
        }

    val derailSec: Long
        get() {
            return TimeUnit.SECONDS.toSeconds(losedate) -
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) -
                    TimeUnit.DAYS.toSeconds(derailDays) -
                    TimeUnit.HOURS.toSeconds(derailHours) -
                    TimeUnit.MINUTES.toSeconds(derailMin)
        }
}