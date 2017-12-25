package me.cpele.beetimer.database

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import me.cpele.beetimer.domain.Status

@Entity
data class StatusContainer(@PrimaryKey val status: Status)
