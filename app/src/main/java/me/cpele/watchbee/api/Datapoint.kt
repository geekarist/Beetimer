package me.cpele.watchbee.api

data class Datapoint(
        val timestamp: String,
        val daystamp: String,
        val value: Double,
        val comment: String,
        val id: String,
        val requestid: String
)