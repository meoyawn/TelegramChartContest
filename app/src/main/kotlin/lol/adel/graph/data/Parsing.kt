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

        reader.forArray {
            beginArray() // nested

            buf.reset()
            val name = nextString()
            while (hasNext()) {
                buf += nextLong()
            }
            map[name] = buf.toArray()

            endArray()
        }

        return Columns(map)
    }

    override fun toJson(writer: JsonWriter, value: Columns?) =
        error("not implemented")
}

private class SimpleArrayMapAdapter<V>(val values: JsonAdapter<V>) : JsonAdapter<SimpleArrayMap<String, V?>>() {

    override fun fromJson(reader: JsonReader): SimpleArrayMap<String, V?> {
        val map = SimpleArrayMap<String, V?>()

        reader.forObject { name ->
            map[name] = values.fromJson(reader)
        }

        return map
    }

    override fun toJson(writer: JsonWriter, value: SimpleArrayMap<String, V?>?): Unit =
        error("not implemented")
}

private class ChartAdapter(moshi: Moshi) : JsonAdapter<Chart>() {

    val columnsAdapter: JsonAdapter<Columns> =
        moshi.adapter<Columns>(Columns::class.java)

    val stringColumnType: JsonAdapter<SimpleArrayMap<String, ColumnType>> =
        moshi.adapter<SimpleArrayMap<String, ColumnType>>(
            Types.newParameterizedType(
                SimpleArrayMap::class.java,
                String::class.java,
                ColumnType::class.java
            )
        )

    val stringString: JsonAdapter<SimpleArrayMap<String, String>> =
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
        var yScaled = false

        reader.forObject { name ->
            when (name) {
                "columns" ->
                    columns = columnsAdapter.fromJson(reader)!!

                "types" ->
                    types = stringColumnType.fromJson(reader)!!

                "names" ->
                    names = stringString.fromJson(reader)!!

                "colors" ->
                    colors = stringString.fromJson(reader)!!

                "percentage" ->
                    percentage = nextBoolean()

                "stacked" ->
                    stacked = nextBoolean()

                "y_scaled" ->
                    yScaled = nextBoolean()
            }
        }

        return Chart(
            columns = columns,
            types = types,
            names = names,
            colors = colors,
            percentage = percentage,
            stacked = stacked,
            y_scaled = yScaled
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
