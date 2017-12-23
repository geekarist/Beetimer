package me.cpele.beetimer.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import me.cpele.beetimer.R
import me.cpele.beetimer.api.Goal

class GoalAdapter: RecyclerView.Adapter<GoalViewHolder>() {

    private var items: MutableList<Goal> = mutableListOf()

    override fun onBindViewHolder(holder: GoalViewHolder?, position: Int) {
        holder?.bind(items[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GoalViewHolder {
        val inflater = LayoutInflater.from(parent?.context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        return GoalViewHolder(itemView)
    }

    override fun onViewRecycled(holder: GoalViewHolder?) {
        holder?.release()
    }

    override fun getItemCount(): Int = items.size

    fun refresh(items: List<Goal>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    fun isEmpty(): Boolean = items.isEmpty()
}