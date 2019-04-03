package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import com.squareup.moshi.*
import help.ColorString
import help.set
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@JsonClass(generateAdapter = false)
enum class ColumnType {
    line,
    x,
}

typealias LineId = String

data class Columns(val map: SimpleArrayMap<LineId, LongArray>)

inline fun JsonReader.loop(until: JsonReader.Token, f: (JsonReader) -> Unit) {
    while (peek() != until) {
        f(this)
    }
}

inline fun JsonReader.array(f: (JsonReader) -> Unit) {
    beginArray()
    f(this)
    endArray()
}

/**
 * iterates over a json object
 */
inline fun JsonReader.forEachKey(f: (name: String, reader: JsonReader) -> Unit) {
    beginObject()
    loop(until = JsonReader.Token.END_OBJECT) {
        f(nextName(), this)
    }
    endObject()
}

/**
 * iterates over a json array
 */
inline fun JsonReader.forEach(f: (JsonReader) -> Unit) {
    beginArray()
    loop(until = JsonReader.Token.END_ARRAY, f = f)
    endArray()
}

private object ColumnsAdapter : JsonAdapter<Columns>() {

    override fun fromJson(reader: JsonReader): Columns {
        val map = SimpleArrayMap<LineId, LongArray>()

        val buf = LongBuffer()

        reader.forEach {
            it.array {
                buf.reset()

                val name = it.nextString()

                it.loop(JsonReader.Token.END_ARRAY) {
                    buf += it.nextLong()
                }

                map[name] = buf.toArray()
            }
        }

        return Columns(map)
    }

    override fun toJson(writer: JsonWriter, value: Columns?) =
        error("not implemented")
}

private class SimpleArrayMapAdapter<V>(private val values: JsonAdapter<V>) : JsonAdapter<SimpleArrayMap<String, V?>>() {

    override fun fromJson(reader: JsonReader): SimpleArrayMap<String, V?> {
        val map = SimpleArrayMap<String, V?>()

        reader.forEachKey { key, r ->
            map[key] = values.fromJson(r)
        }

        return map
    }

    override fun toJson(writer: JsonWriter, value: SimpleArrayMap<String, V?>?): Unit =
        error("not implemented")
}

class LongBuffer {

    private var buf = LongArray(size = 12)
    private var size: Int = 0

    operator fun plusAssign(l: Long) {
        if (buf.size == size) {
            val new = LongArray(size = buf.size * 2)
            buf.copyInto(new)
            buf = new
        }
        buf[size] = l
        size++
    }

    fun reset() {
        size = 0
    }

    fun toArray(): LongArray =
        LongArray(size).also {
            buf.copyInto(it, endIndex = size)
        }
}

object Chart1AdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when {
            type == Chart1::class.java ->
                Chart1JsonAdapter(moshi)

            type == Columns::class.java ->
                ColumnsAdapter

            type is ParameterizedType && type.rawType == SimpleArrayMap::class.java ->
                SimpleArrayMapAdapter<Any>(moshi.adapter(type.actualTypeArguments.first()))

            else ->
                null
        }
}

@JsonClass(generateAdapter = true)
data class Chart1(
    val columns: Columns,
    val types: SimpleArrayMap<LineId, ColumnType>,
    val names: SimpleArrayMap<LineId, String>,
    val colors: SimpleArrayMap<LineId, ColorString>
)
