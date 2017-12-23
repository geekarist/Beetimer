package me.cpele.beetimer.repository

import me.cpele.beetimer.api.Goal

data class LoadingSuccessEvent(val goals: List<Goal>)