package me.cpele.fleabrainer.ui

import android.app.TimePickerDialog
import android.content.Context
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.TimePicker
import kotlinx.android.synthetic.main.view_item.view.*
import me.cpele.fleabrainer.databinding.ViewItemBinding
import me.cpele.fleabrainer.domain.GoalTiming
import me.cpele.fleabrainer.domain.Stopwatch

class GoalGeneralViewHolder(
        private val itemBinding: ViewItemBinding,
        private val listener: Listener
) : GoalViewListener {

    private val context: Context
        get() = itemBinding.root.context

    private lateinit var stopwatch: Stopwatch

    private var handler: Handler = Handler()
    private var runnable: Runnable? = null

    fun bind(goalTiming: GoalTiming) {

        itemBinding.model = goalTiming
        itemBinding.listener = this

        stopwatch = goalTiming.stopwatch

        attach()
    }

    fun attach() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable {
            itemBinding.root.item_timer.text = stopwatch.format()
            itemBinding.root.item_timer.setTextColor(ContextCompat.getColor(context, stopwatch.color()))
            handler.postDelayed(runnable, 1000)
        }
        handler.post(runnable)
    }

    override fun onClickItem(goalTiming: GoalTiming) {
        listener.onOpen(goalTiming)
    }

    override fun onClickTimer(goalTiming: GoalTiming) =
        listener.toggleThenStopOthers(goalTiming.goal.slug)

    override fun onLongClickTimer(goalTiming: GoalTiming): Boolean {
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
        return true
    }

    override fun onClickReset(goalTiming: GoalTiming) {
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

    override fun onClickSubmit(goalTiming: GoalTiming) {
        listener.onSubmit(goalTiming)
    }

    fun detach() {
        runnable?.let { handler.removeCallbacks(it) }
    }

    interface Listener {
        fun onPersist(goalTiming: GoalTiming)
        fun onSubmit(goalTiming: GoalTiming)
        fun onOpen(goalTiming: GoalTiming)
        fun toggleThenStopOthers(slug: String)
    }
}