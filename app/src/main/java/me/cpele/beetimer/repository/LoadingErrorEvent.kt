package me.cpele.beetimer.repository

data class LoadingErrorEvent(val message: String, val cause: Throwable?)