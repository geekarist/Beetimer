package me.cpele.fleabrainer.ui

import me.cpele.fleabrainer.domain.GoalTiming

interface GoalViewListener {

    fun onClickItem(goalTiming: GoalTiming)
    fun onClickTimer(goalTiming: GoalTiming)
    fun onLongClickTimer(goalTiming: GoalTiming): Boolean
    fun onClickReset(goalTiming: GoalTiming)
    fun onClickSubmit(goalTiming: GoalTiming)
}
