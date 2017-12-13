package me.cpele.beetimer

import android.content.Context
import android.util.AttributeSet
import android.widget.Chronometer

class ChronometerWithStatus(
        context: Context,
        attributes: AttributeSet
) : Chronometer(context, attributes) {

    var running: Boolean = false
        private set

    override fun start() {
        super.start()
        running = true
    }

    override fun stop() {
        super.stop()
        running = false
    }
}