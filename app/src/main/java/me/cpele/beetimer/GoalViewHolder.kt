package me.cpele.beetimer

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import java.util.concurrent.TimeUnit

class GoalViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
    fun bind(goal: Goal) {
        val idView: TextView = itemView.findViewById(R.id.item_id)
        idView.text = goal.slug

        val titleView: TextView = itemView.findViewById(R.id.item_title)
        titleView.text = goal.title

        val rateView: TextView = itemView.findViewById(R.id.item_rate)
        rateView.text = goal.rate

        val bareMinView: TextView = itemView.findViewById(R.id.item_bare_min)
        bareMinView.text = goal.delta_text

        @Suppress("DEPRECATION")
        goal.losedate?.let {

            val color = goal.color

            val numDerailDays =
                    TimeUnit.SECONDS.toDays(goal.losedate) -
                            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            val derailDaysView: TextView = itemView.findViewById(R.id.item_derail_days)
            derailDaysView.text = numDerailDays.toString()
            derailDaysView.setBackgroundColor(itemView.context.resources.getColor(color))

            val numDerailHours =
                    TimeUnit.SECONDS.toHours(goal.losedate) -
                            TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis()) -
                            TimeUnit.DAYS.toHours(numDerailDays)
            val derailHoursView: TextView = itemView.findViewById(R.id.item_derail_time)
            val numDerailMin = 51
            val numDerailSec = 16
            derailHoursView.text = itemView.context.getString(
                    R.string.item_derail_time, numDerailHours, numDerailMin, numDerailSec)
            derailHoursView.setBackgroundColor(itemView.context.resources.getColor(color))
        }
    }
}