package me.cpele.watchbee.ui

import android.app.TimePickerDialog
import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.TimePicker
import kotlinx.android.synthetic.main.view_item.view.*
import me.cpele.watchbee.databinding.ViewItemBinding
import me.cpele.watchbee.domain.GoalTiming
import me.cpele.watchbee.domain.Stopwatch

class GoalViewHolder(
        private val itemBinding: ViewItemBinding,
        private val listener: Listener
) : RecyclerView.ViewHolder(itemBinding.root) {

    private val context: Context
        get() = itemView.context

    private lateinit var stopwatch: Stopwatch

    private var handler: Handler = Handler()
    private var runnable: Runnable? = null

    fun bind(goalTiming: GoalTiming) {

        itemBinding.model = goalTiming.goal

        stopwatch = goalTiming.stopwatch
        itemView.item_timer.setOnClickListener {
            stopwatch.toggle()
            listener.onPersist(goalTiming)
        }
        itemView.item_timer.setOnLongClickListener {
            val wasRunning = stopwatch.running
            stopwatch.stop()
            val timePickerDialog = TimePickerDialog(
                    context,
                    { _: TimePicker, hours: Int, minutes: Int ->
                        stopwatch.set(hours, minutes)
                        if (wasRunning) stopwatch.start()
                        listener.onPersist(goalTiming)
                    },
                    stopwatch.hours,
                    stopwatch.minutes,
                    true
            )
            timePickerDialog.setOnCancelListener {
                if (wasRunning) stopwatch.start()
                listener.onPersist(goalTiming)
            }
            timePickerDialog.show()
            true
        }

        attach()

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

    fun attach() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable {
            Log.d(javaClass.simpleName, "Updating stopwatch")
            itemView.item_timer.text = stopwatch.format()
            itemView.item_timer.setTextColor(ContextCompat.getColor(context, stopwatch.color()))
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }

    fun detach() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    interface Listener {
        fun onPersist(goalTiming: GoalTiming)
        fun onSubmit(goalTiming: GoalTiming)
    }
}