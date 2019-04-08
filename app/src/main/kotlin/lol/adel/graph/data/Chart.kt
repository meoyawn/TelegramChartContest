package lol.adel.graph.data

import androidx.annotation.MainThread
import androidx.collection.SimpleArrayMap
import com.squareup.moshi.JsonClass
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.set
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@JsonClass(generateAdapter = false)
enum class ColumnType {
    line,
    x,
    area,
    bar,
}

typealias LineId = String

data class Columns(val map: SimpleArrayMap<LineId, LongArray>)

fun Columns.first(): LongArray =
    map.first()

operator fun Columns.get(id: LineId?): LongArray =
    map[id] ?: error("no column for $id")

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

fun Chart.color(id: LineId): ColorInt =
    parseColor(colors[id] ?: error("color not found for $id"))

inline fun Chart.forEachIndex(f: (Idx) -> Unit) {
    for (i in 0 until size()) {
        f(i)
    }
}

fun Chart.size(): Int =
    columns.first().size

operator fun Chart.get(id: LineId): LongArray =
    columns[id]

fun Chart.lineIds(): List<LineId> =
    types.filterKeys { _, type -> type != ColumnType.x }

fun Chart.xs(): LongArray =
    columns[types.findKey { _, type -> type == ColumnType.x }]

private val MM = MinMax()

@MainThread
fun Chart.minMax(cameraX: MinMax, lines: List<LineId>): MinMax {

    val minX = 0
    val maxX = size() - 1

    val begin = clamp(cameraX.min.ceil(), minX, maxX)
    val end = clamp(cameraX.max.floor(), minX, maxX)

    var minY = Long.MAX_VALUE
    var maxY = Long.MIN_VALUE

    if (stacked) {
        minY = 0L
        for (i in begin..end) {
            maxY = max(maxY, lines.sumByIndex { this[it][i] })
        }
    } else {
        lines.forEachByIndex { id ->
            val points = this[id]
            for (i in begin..end) {
                val p = points[i]
                minY = min(minY, p)
                maxY = max(maxY, p)
            }
        }
    }

    MM.set(min = minY.toFloat(), max = maxY.toFloat())
    return MM
}

fun chartName(idx: Idx): String =
    "Chart ${idx + 1}"

private fun rnd(value: Double): Double =
    round(value * 100) / 100

fun chartValue(value: Long, max: Float): String =
    when (abs(value)) {
        in 0..1_000 ->
            "$value"

        in 0..1_000_000 ->
            if (max < 1_000_000) "$value"
            else "${value / 1_000}K"

        else ->
            "${rnd(value = value / 1_000_000.0)}M"
    }
