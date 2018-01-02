package me.cpele.watchbee.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cpele.watchbee.R
import me.cpele.watchbee.domain.GoalTiming

class GoalAdapter(private val listener: GoalViewHolder.Listener) : RecyclerView.Adapter<GoalViewHolder>() {

    private var items: MutableList<GoalTiming> = mutableListOf()

    override fun onBindViewHolder(holder: GoalViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GoalViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        return GoalViewHolder(itemView, listener)
    }

    override fun onViewRecycled(holder: GoalViewHolder?) {
        holder?.release()
    }

    override fun getItemCount(): Int = items.size

    fun refresh(items: List<GoalTiming>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean = items.isEmpty()
}