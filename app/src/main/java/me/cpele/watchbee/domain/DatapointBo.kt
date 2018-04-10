package me.cpele.watchbee.domain

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "PendingDatapoint")
data class DatapointBo(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val userName: String,
        val goalSlug: String,
        val datapointValue: Float,
        val comment: String,
        var pending: Boolean
)