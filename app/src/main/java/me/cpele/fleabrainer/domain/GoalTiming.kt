package me.cpele.fleabrainer.domain

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import me.cpele.fleabrainer.api.Goal

@Entity
data class GoalTiming(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user: String,
    @Embedded var goal: Goal,
    @Embedded val stopwatch: Stopwatch
) : Comparable<GoalTiming> {

    override fun compareTo(other: GoalTiming): Int = stopwatch.compareTo(other.stopwatch)
}