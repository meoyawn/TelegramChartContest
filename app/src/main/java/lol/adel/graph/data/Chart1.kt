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

data class Columns(val map: SimpleArrayMap<LineId, List<Long>>)

private object ColumnsAdapter : JsonAdapter<Columns>() {

    override fun fromJson(reader: JsonReader): Columns {
        val map = SimpleArrayMap<LineId, List<Long>>()
        reader.beginArray()

        while (reader.peek() != JsonReader.Token.END_ARRAY) {
            val buf = ArrayList<Long>()

            reader.beginArray()

            val name = reader.nextString()
            while (reader.peek() != JsonReader.Token.END_ARRAY) {
                buf += reader.nextLong()
            }
            map[name] = buf

            reader.endArray()
        }

        reader.endArray()
        return Columns(map)
    }

    override fun toJson(writer: JsonWriter, value: Columns?) =
        error("not implemented")
}

private class SimpleArrayMapAdapter<V>(private val values: JsonAdapter<V>) : JsonAdapter<SimpleArrayMap<String, V?>>() {

    override fun fromJson(reader: JsonReader): SimpleArrayMap<String, V?> {
        val map = SimpleArrayMap<String, V?>()

        reader.beginObject()
        while (reader.peek() != JsonReader.Token.END_OBJECT) {
            map[reader.nextName()] = values.fromJson(reader)
        }
        reader.endObject()

        return map
    }

    override fun toJson(writer: JsonWriter, value: SimpleArrayMap<String, V?>?): Unit =
        error("not implemented")
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
