package me.cpele.fleabrainer.domain

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity
data class StatusChange(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val date: Date = Date(),
        val status: Status,
        val message: String? = null
)
