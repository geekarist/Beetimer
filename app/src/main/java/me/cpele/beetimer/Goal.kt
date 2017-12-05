package me.cpele.beetimer

import android.support.annotation.ColorRes

data class Goal(
        val slug: String?,
        val title: String?,
        val rate: String?,
        val delta_text: String?,
        val losedate: Long?,
        private val lane: Int?,
        private val yaw: Int?,
        val runits: String?) {

    val color: Int
        @ColorRes
        get() {
            if (lane == null || yaw == null) return android.R.color.white

            val laneTimesYaw = lane * yaw
            if (laneTimesYaw > 1) return R.color.green
            else if (laneTimesYaw == 1) return R.color.blue
            else if (laneTimesYaw == -1) return R.color.orange
            else if (laneTimesYaw <= -2) return R.color.red
            return android.R.color.white
        }
}