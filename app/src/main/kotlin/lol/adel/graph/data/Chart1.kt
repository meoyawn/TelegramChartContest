package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import com.squareup.moshi.*
import help.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

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

private class ChartJsonAdapter(moshi: Moshi) : JsonAdapter<Chart>() {

    private val options: JsonReader.Options =
        JsonReader.Options.of("columns", "types", "names", "colors")

    private val columnsAdapter: JsonAdapter<Columns> =
        moshi.adapter<Columns>(Columns::class.java, kotlin.collections.emptySet(), "columns")

    private val simpleArrayMapOfStringColumnTypeAdapter: JsonAdapter<SimpleArrayMap<String, ColumnType>> =
        moshi.adapter<SimpleArrayMap<String, ColumnType>>(
            Types.newParameterizedType(
                SimpleArrayMap::class.java,
                String::class.java,
                ColumnType::class.java
            ), kotlin.collections.emptySet(), "types"
        )

    private val simpleArrayMapOfStringStringAdapter: JsonAdapter<SimpleArrayMap<String, String>> =
        moshi.adapter<SimpleArrayMap<String, String>>(
            Types.newParameterizedType(
                SimpleArrayMap::class.java,
                String::class.java,
                String::class.java
            ), kotlin.collections.emptySet(), "names"
        )

    override fun toString(): String = "GeneratedJsonAdapter(Chart)"

    override fun fromJson(reader: JsonReader): Chart {
        var columns: Columns? = null
        var types_: SimpleArrayMap<String, ColumnType>? = null
        var names: SimpleArrayMap<String, String>? = null
        var colors: SimpleArrayMap<String, String>? = null
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(options)) {
                0 -> columns = columnsAdapter.fromJson(reader)
                    ?: throw JsonDataException("Non-null value 'columns' was null at ${reader.path}")
                1 -> types_ = simpleArrayMapOfStringColumnTypeAdapter.fromJson(reader)
                    ?: throw JsonDataException("Non-null value 'types_' was null at ${reader.path}")
                2 -> names = simpleArrayMapOfStringStringAdapter.fromJson(reader)
                    ?: throw JsonDataException("Non-null value 'names' was null at ${reader.path}")
                3 -> colors = simpleArrayMapOfStringStringAdapter.fromJson(reader)
                    ?: throw JsonDataException("Non-null value 'colors' was null at ${reader.path}")
                -1 -> {
                    // Unknown name, skip it.
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
        return Chart(
            columns = columns ?: throw JsonDataException("Required property 'columns' missing at ${reader.path}"),
            types = types_ ?: throw JsonDataException("Required property 'types_' missing at ${reader.path}"),
            names = names ?: throw JsonDataException("Required property 'names' missing at ${reader.path}"),
            colors = colors ?: throw JsonDataException("Required property 'colors' missing at ${reader.path}")
        )
    }

    override fun toJson(writer: JsonWriter, value: Chart?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        writer.beginObject()
        writer.name("columns")
        columnsAdapter.toJson(writer, value.columns)
        writer.name("types")
        simpleArrayMapOfStringColumnTypeAdapter.toJson(writer, value.types)
        writer.name("names")
        simpleArrayMapOfStringStringAdapter.toJson(writer, value.names)
        writer.name("colors")
        simpleArrayMapOfStringStringAdapter.toJson(writer, value.colors)
        writer.endObject()
    }
}

object ChartAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when {
            type == Chart::class.java ->
                ChartJsonAdapter(moshi)

            type == Columns::class.java ->
                ColumnsAdapter

            type is ParameterizedType && type.rawType == SimpleArrayMap::class.java ->
                SimpleArrayMapAdapter<Any>(moshi.adapter(type.actualTypeArguments.last()))

            else ->
                null
        }
}
