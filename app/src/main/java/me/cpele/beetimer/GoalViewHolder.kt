package me.cpele.beetimer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

class GoalViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun bind(goal: Goal) {
        val idView: TextView = itemView.findViewById(R.id.item_id)
        idView.text = goal.slug

        val titleview: TextView = itemView.findViewById(R.id.item_title)
        titleview.text = goal.title
    }
}