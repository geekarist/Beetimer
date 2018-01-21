package me.cpele.watchbee.api

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class User(
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val username: String,
        // TODO create a proper entity
        val goals: ArrayList<String>
)