package me.cpele.beetimer.domain

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import me.cpele.beetimer.api.Goal

@Entity
data class GoalTiming(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        @Embedded var goal: Goal,
        @Embedded val stopwatch: Stopwatch
)