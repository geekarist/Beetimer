package me.cpele.watchbee.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*
import java.util.concurrent.TimeUnit

class EpochTypeAdapter : TypeAdapter<Date>() {

    override fun write(out: JsonWriter?, value: Date?) {
        val milTime = value?.time ?: 0
        val secTime = TimeUnit.MILLISECONDS.toSeconds(milTime)
        out?.value(secTime)
    }

    override fun read(input: JsonReader?): Date {
        return input?.nextLong()?.let {
            Date(TimeUnit.SECONDS.toMillis(it))
        } ?: Date(0)
    }
}
