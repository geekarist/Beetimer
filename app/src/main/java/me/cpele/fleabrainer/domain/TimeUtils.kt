package me.cpele.fleabrainer.domain

import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

/**
 * Convert hours to a duration.
 *
 * Example:
 *
 * ```
 * val duration = formatHoursAsDuration(.75)
 * ```
 *
 * Here, `duration` will be "00:45:00" (45 minutes).
 */
fun formatHoursAsDuration(floatHours: Float, displaySign: Boolean = true): String {
    val floatMsDuration = floatHours * 60 * 60 * 1000
    val longMsDuration = floatMsDuration.toLong()
    val hrDuration = TimeUnit.MILLISECONDS.toHours(longMsDuration)
    val msHrDuration = TimeUnit.HOURS.toMillis(hrDuration)
    val minDuration = TimeUnit.MILLISECONDS.toMinutes(longMsDuration - msHrDuration)
    val msMinDuration = TimeUnit.MINUTES.toMillis(minDuration)
    val secDuration = TimeUnit.MILLISECONDS.toSeconds(longMsDuration - msHrDuration - msMinDuration)

    var result = ""

    if (floatHours < 0) {
        result += "-"
    } else if (displaySign) {
        result += "+"
    }

    result += "%02d:%02d:%02d".format(
            hrDuration.absoluteValue,
            minDuration.absoluteValue,
            secDuration.absoluteValue
    )

    return result
}