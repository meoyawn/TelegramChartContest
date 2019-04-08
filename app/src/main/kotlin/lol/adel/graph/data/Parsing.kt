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

                val name = nextString()

                while (hasNext()) {
                    buf += nextLong()
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

private class ChartAdapter(moshi: Moshi) : JsonAdapter<Chart>() {

    private val columnsAdapter: JsonAdapter<Columns> =
        moshi.adapter<Columns>(Columns::class.java)

    private val simpleArrayMapOfStringColumnTypeAdapter: JsonAdapter<SimpleArrayMap<String, ColumnType>> =
        moshi.adapter<SimpleArrayMap<String, ColumnType>>(
            Types.newParameterizedType(
                SimpleArrayMap::class.java,
                String::class.java,
                ColumnType::class.java
            )
        )

    private val simpleArrayMapOfStringStringAdapter: JsonAdapter<SimpleArrayMap<String, String>> =
        moshi.adapter<SimpleArrayMap<String, String>>(
            Types.newParameterizedType(
                SimpleArrayMap::class.java,
                String::class.java,
                String::class.java
            )
        )

    override fun fromJson(reader: JsonReader): Chart {
        lateinit var columns: Columns
        lateinit var types: SimpleArrayMap<String, ColumnType>
        lateinit var names: SimpleArrayMap<String, String>
        lateinit var colors: SimpleArrayMap<String, String>

        var percentage = false
        var stacked = false
        var y_scaled = false

        reader.forEachKey { name, _ ->
            when (name) {
                "columns" ->
                    columns = columnsAdapter.fromJson(reader)!!

                "types" ->
                    types = simpleArrayMapOfStringColumnTypeAdapter.fromJson(reader)!!

                "names" ->
                    names =
                        simpleArrayMapOfStringStringAdapter.fromJson(reader)!!

                "colors" ->
                    colors =
                        simpleArrayMapOfStringStringAdapter.fromJson(reader)!!

                "percentage" ->
                    percentage = reader.nextBoolean()

                "stacked" ->
                    stacked = reader.nextBoolean()

                "y_scaled" ->
                    y_scaled = reader.nextBoolean()
            }
        }

        return Chart(
            columns = columns,
            types = types,
            names = names,
            colors = colors,
            percentage = percentage,
            stacked = stacked,
            y_scaled = y_scaled
        )
    }

    override fun toJson(writer: JsonWriter, value: Chart?) =
        error("not implemented")
}

object ChartAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when {
            type == Chart::class.java ->
                ChartAdapter(moshi)

            type == Columns::class.java ->
                ColumnsAdapter

            type is ParameterizedType && type.rawType == SimpleArrayMap::class.java ->
                SimpleArrayMapAdapter<Any>(moshi.adapter(type.actualTypeArguments.last()))

            else ->
                null
        }
}
