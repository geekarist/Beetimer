package me.cpele.watchbee.api

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.util.*

class EpochTypeAdapter : TypeAdapter<Date>() {

    override fun write(out: JsonWriter?, value: Date?) {
        out?.value(value?.time)
    }

    override fun read(input: JsonReader?): Date {
        return input?.nextLong()?.let { Date(it) } ?: Date()
    }
}
