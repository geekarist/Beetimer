package me.cpele.fleabrainer.domain

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.util.*

@Entity(tableName = "Datapoint")
data class DatapointBo(
        @PrimaryKey
        var id: String,
        val goalSlug: String,
        val userName: String,
        val datapointValue: Float,
        val comment: String,
        var pending: Boolean,
        val updatedAt: Date
)