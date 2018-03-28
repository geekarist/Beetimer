package me.cpele.watchbee.ui

import me.cpele.watchbee.domain.GoalTiming

interface GoalViewListener {

    fun onClickItem(goalTiming: GoalTiming)
    fun onClickTimer(goalTiming: GoalTiming)
    fun onLongClickTimer(goalTiming: GoalTiming): Boolean
    fun onClickReset(goalTiming: GoalTiming)
    fun onClickSubmit(goalTiming: GoalTiming)
}
