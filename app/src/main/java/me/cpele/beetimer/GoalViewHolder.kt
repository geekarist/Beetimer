package me.cpele.beetimer

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_item.view.*
import java.util.concurrent.TimeUnit

class GoalViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    private val mContext: Context
        get() = itemView.context

    fun bind(goal: Goal) {
        itemView.item_id.text = goal.slug
        itemView.item_title.text = goal.title
        itemView.item_rate.text = mContext.getString(R.string.item_rate, goal.rate, goal.runits)
        itemView.item_bare_min.text = goal.limsum

        val timerView = itemView.item_timer
        timerView.setOnClickListener {
            if (timerView.running) timerView.stop() else timerView.start()
        }

        @Suppress("DEPRECATION")
        goal.losedate?.let {

            val color = goal.color

            val numDerailDays =
                    TimeUnit.SECONDS.toDays(goal.losedate) -
                            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
            itemView.item_derail_days.text = mContext.getString(R.string.item_derail_date, numDerailDays)
            itemView.item_derail_days.setBackgroundColor(mContext.resources.getColor(color))

            val numDerailHours =
                    TimeUnit.SECONDS.toHours(goal.losedate) -
                            TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis()) -
                            TimeUnit.DAYS.toHours(numDerailDays)

            val numDerailMin = TimeUnit.SECONDS.toMinutes(goal.losedate) -
                    TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()) -
                    TimeUnit.DAYS.toMinutes(numDerailDays) -
                    TimeUnit.HOURS.toMinutes(numDerailHours)

            val numDerailSec = TimeUnit.SECONDS.toSeconds(goal.losedate) -
                    TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) -
                    TimeUnit.DAYS.toSeconds(numDerailDays) -
                    TimeUnit.HOURS.toSeconds(numDerailHours) -
                    TimeUnit.MINUTES.toSeconds(numDerailMin)

            itemView.item_derail_time.text = mContext.getString(
                    R.string.item_derail_time, numDerailHours, numDerailMin, numDerailSec)

            itemView.item_derail_time.setBackgroundColor(mContext.resources.getColor(color))
        }
    }
}