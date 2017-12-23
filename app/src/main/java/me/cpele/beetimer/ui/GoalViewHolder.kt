package me.cpele.beetimer.ui

import android.content.Context
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_item.view.*
import me.cpele.beetimer.R
import me.cpele.beetimer.api.Goal
import me.cpele.beetimer.domain.StopWatch
import java.util.concurrent.TimeUnit

class GoalViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    private val context: Context
        get() = itemView.context

    private lateinit var stopWatch: StopWatch

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    fun bind(goal: Goal) {
        itemView.item_id.text = goal.slug
        itemView.item_title.text = goal.title
        itemView.item_rate.text = context.getString(R.string.item_rate, goal.rate, goal.runits)
        itemView.item_bare_min.text = goal.limsum

        stopWatch = StopWatch()
        itemView.item_timer.setOnClickListener { stopWatch.toggle() }
        handler = Handler()
        runnable = object : Runnable {
            override fun run() {
                itemView.item_timer.text = stopWatch.format()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        val color = goal.color

        val numDerailDays =
                TimeUnit.SECONDS.toDays(goal.losedate) -
                        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        itemView.item_derail_days.text = context.getString(R.string.item_derail_date, numDerailDays)
        @Suppress("DEPRECATION")
        itemView.item_derail_days.setBackgroundColor(context.resources.getColor(color))

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

        itemView.item_derail_time.text = context.getString(
                R.string.item_derail_time, numDerailHours, numDerailMin, numDerailSec)

        @Suppress("DEPRECATION")
        itemView.item_derail_time.setBackgroundColor(context.resources.getColor(color))
    }

    fun release() {
        handler.removeCallbacks(runnable)
    }
}