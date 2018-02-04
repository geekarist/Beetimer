package me.cpele.watchbee.ui

import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.view_item.view.*
import me.cpele.watchbee.R
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.Stopwatch
import java.util.concurrent.TimeUnit

class GoalViewHolder(
        itemView: View?,
        private val listener: Listener
) : RecyclerView.ViewHolder(itemView) {

    private val context: Context
        get() = itemView.context

    private lateinit var stopwatch: Stopwatch

    private var handler: Handler = Handler()
    private var runnable: Runnable? = null

    fun bind(goalTiming: GoalTiming) {

        val goal = goalTiming.goal

        itemView.item_id.text = goal.slug
        itemView.item_title.text = goal.title
        itemView.item_rate.text = context.getString(R.string.item_rate, goal.rate, goal.runits)
        itemView.item_bare_min.text = goal.limsum

        stopwatch = goalTiming.stopwatch
        itemView.item_timer.setOnClickListener {
            stopwatch.toggle()
            listener.onPersist(goalTiming)
        }
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable {
            itemView.item_timer.text = stopwatch.format()
            itemView.item_timer.setTextColor(ContextCompat.getColor(context, stopwatch.color()))
            handler.postDelayed(runnable, 1000)
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

        itemView.item_reset.setOnClickListener {
            AlertDialog.Builder(context)
                    .setMessage("Do you really want to reset this stopwatch?")
                    .setPositiveButton(android.R.string.ok, { _, _ ->
                        stopwatch.clear()
                        listener.onPersist(goalTiming)
                    })
                    .setNeutralButton(android.R.string.cancel, { dialog, _ ->
                        dialog.cancel()
                    })
                    .show()
        }

        itemView.item_submit.setOnClickListener {
            listener.onSubmit(goalTiming)
        }
    }

    fun release() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    interface Listener {
        fun onPersist(goalTiming: GoalTiming)
        fun onSubmit(goalTiming: GoalTiming)
    }
}