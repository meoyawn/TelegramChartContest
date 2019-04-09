package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import com.squareup.moshi.JsonClass
import help.any
import help.filterKeys
import help.findKey

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

enum class ChartType {
    LINE,
    TWO_Y,
    BAR,
    AREA,
}

@JsonClass(generateAdapter = true)
data class Chart(
    val columns: Columns,
    val types: SimpleArrayMap<LineId, ColumnType>,
    val names: SimpleArrayMap<LineId, String>,
    val colors: SimpleArrayMap<LineId, ColorString>,
    val percentage: Boolean,
    val stacked: Boolean,
    val y_scaled: Boolean // two y axes
) {
    val xs: LongArray = columns[types.findKey { _, t -> t == ColumnType.x }]

    val lineIds: List<LineId> = types.filterKeys { _, t -> t != ColumnType.x }

    val size: Int = columns.first().size

    val type: ChartType = when {
        y_scaled ->
            ChartType.TWO_Y

        percentage ->
            ChartType.AREA

        types.any { _, t -> t == ColumnType.bar } ->
            ChartType.BAR

        else ->
            ChartType.LINE
    }
}
