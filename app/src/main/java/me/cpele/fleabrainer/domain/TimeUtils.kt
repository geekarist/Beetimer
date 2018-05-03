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
    val floatMsRate = floatHours * 60 * 60 * 1000
    val longMsRate = floatMsRate.toLong()
    val hrRate = TimeUnit.MILLISECONDS.toHours(longMsRate)
    val msHrRate = TimeUnit.HOURS.toMillis(hrRate)
    val minRate = TimeUnit.MILLISECONDS.toMinutes(longMsRate - msHrRate)

    var result = ""
    if (displaySign) result += if (floatHours >= 0) "+" else "-"
    if (hrRate > 0) result += "${hrRate}h"
    if (minRate > 0) result += "${minRate}m"
    return result
}