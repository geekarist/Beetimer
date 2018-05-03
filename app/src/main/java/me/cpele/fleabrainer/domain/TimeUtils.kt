package me.cpele.fleabrainer.domain

import java.util.concurrent.TimeUnit

/**
 * Convert hours to a duration.
 *
 * Example:
 *
 * ```
 * val duration = formatHoursAsDuration(.75)
 * ```
 *
 * In that case, `duration` is "45m" (45 minutes).
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
    if (displaySign) result += if (floatHours >= 0) "+" else "-"
    if (hrDuration > 0) result += "${hrDuration}h"
    if (hrDuration > 0 || minDuration > 0) result += "${minDuration}m"
    result += "${secDuration}s"
    return result
}