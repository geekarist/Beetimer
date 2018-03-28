package me.cpele.watchbee.ui

import android.support.v7.widget.RecyclerView
import me.cpele.watchbee.databinding.ViewItemBinding
import me.cpele.watchbee.domain.GoalTiming

class GoalRecyclerViewHolder(
        private val binding: ViewItemBinding,
        listener: GoalGeneralViewHolder.Listener
) : RecyclerView.ViewHolder(binding.root), GoalViewListener {

    private var generalViewHolder = GoalGeneralViewHolder(binding, listener)

    fun bind(goalTiming: GoalTiming) {
        generalViewHolder.bind(goalTiming)
    }

    fun attach() = generalViewHolder.attach()

    override fun onClickItem(goalTiming: GoalTiming) = generalViewHolder.onClickItem(goalTiming)
    override fun onClickTimer(goalTiming: GoalTiming) = generalViewHolder.onClickTimer(goalTiming)
    override fun onClickReset(goalTiming: GoalTiming) = generalViewHolder.onClickReset(goalTiming)
    override fun onClickSubmit(goalTiming: GoalTiming) = generalViewHolder.onClickSubmit(goalTiming)

    override fun onLongClickTimer(goalTiming: GoalTiming): Boolean {
        return generalViewHolder.onLongClickTimer(goalTiming)
    }

    fun detach() {
        generalViewHolder.detach()
    }
}