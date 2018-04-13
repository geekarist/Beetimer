package me.cpele.watchbee.api

import java.util.*

data class Datapoint(
        val timestamp: String,
        val daystamp: String,
        val value: Double,
        val comment: String,
        val id: String,
        val requestid: String,
        val updated_at: Date
)