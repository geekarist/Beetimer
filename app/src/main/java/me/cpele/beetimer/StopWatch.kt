package me.cpele.beetimer

class StopWatch {

    private var startTime: Long = 0
    private var stopTime: Long = 0

    private var running: Boolean = false

    private var elapsedPreviously: Long = 0

    fun elapsedMillis(): Long =
            elapsedPreviously +
                    if (running) System.currentTimeMillis() - startTime
                    else stopTime - startTime

    fun toggle() {
        if (!running) {
            elapsedPreviously += stopTime - startTime
            startTime = System.currentTimeMillis()
            running = true
        } else {
            stopTime = System.currentTimeMillis()
            running = false
        }
    }
}