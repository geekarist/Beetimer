package me.cpele.fleabrainer.ui

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cpele.fleabrainer.R
import me.cpele.fleabrainer.databinding.ViewItemBinding
import me.cpele.fleabrainer.domain.GoalTiming

class GoalAdapter(private val listener: GoalGeneralViewHolder.Listener) :
    ListAdapter<GoalTiming, GoalRecyclerViewHolder>(CustomItemCallback) {

    object CustomItemCallback : DiffUtil.ItemCallback<GoalTiming>() {
        override fun areItemsTheSame(oldItem: GoalTiming, newItem: GoalTiming): Boolean {
            return (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: GoalTiming, newItem: GoalTiming): Boolean {
            return (oldItem == newItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalRecyclerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        val itemBinding = ViewItemBinding.bind(itemView)
        return GoalRecyclerViewHolder(itemBinding, listener)
    }

    override fun onBindViewHolder(holder: GoalRecyclerViewHolder, position: Int) {
        val goalTiming = getItem(position)
        holder.bind(goalTiming)
    }

    override fun onViewAttachedToWindow(holder: GoalRecyclerViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }

    override fun onViewRecycled(holder: GoalRecyclerViewHolder) {
        holder.detach()
    }

    override fun onViewDetachedFromWindow(holder: GoalRecyclerViewHolder) {
        holder.detach()
        super.onViewDetachedFromWindow(holder)
    }

    /**
     * Find the position of the first running item.
     *
     * @return the position if an item is running, or else `null`
     */
    fun firstRunningItemPosition(): Int? {
        for (i in 0 until itemCount) {
            val item = getItem(i)
            if (item.stopwatch.running) {
                return i
            }
        }
        return null
    }
}