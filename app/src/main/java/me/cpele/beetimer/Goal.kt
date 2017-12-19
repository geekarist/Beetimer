package me.cpele.beetimer

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.ColorRes

@Entity
data class Goal(
        @PrimaryKey
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
}