package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
enum class ColumnType {
    line,
    x,
    area,
    bar,
}

typealias LineId = String

data class Columns(val map: SimpleArrayMap<LineId, LongArray>)

typealias ColorString = String

@JsonClass(generateAdapter = true)
data class Chart(
    val columns: Columns,
    val types: SimpleArrayMap<LineId, ColumnType>,
    val names: SimpleArrayMap<LineId, String>,
    val colors: SimpleArrayMap<LineId, ColorString>,
    val percentage: Boolean,
    val stacked: Boolean,
    val y_scaled: Boolean // two y axes
)
