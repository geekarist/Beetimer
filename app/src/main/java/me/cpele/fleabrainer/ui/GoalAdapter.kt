package me.cpele.fleabrainer.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cpele.fleabrainer.R
import me.cpele.fleabrainer.databinding.ViewItemBinding
import me.cpele.fleabrainer.domain.GoalTiming

class GoalAdapter(private val listener: GoalGeneralViewHolder.Listener)
    : RecyclerView.Adapter<GoalRecyclerViewHolder>() {

    private var items: MutableList<GoalTiming> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalRecyclerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        val itemBinding = ViewItemBinding.bind(itemView)
        return GoalRecyclerViewHolder(itemBinding, listener)
    }

    override fun onBindViewHolder(holder: GoalRecyclerViewHolder, position: Int) {
        val goalTiming = items[position]
        holder.bind(goalTiming)
    }

    override fun onViewAttachedToWindow(holder: GoalRecyclerViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.attach()
    }

    override fun onViewRecycled(holder: GoalRecyclerViewHolder) {
        holder.detach()
    }

    override fun getItemCount(): Int = items.size

    fun refresh(items: List<GoalTiming>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean = items.isEmpty()

    override fun onViewDetachedFromWindow(holder: GoalRecyclerViewHolder) {
        holder.detach()
        super.onViewDetachedFromWindow(holder)
    }
}