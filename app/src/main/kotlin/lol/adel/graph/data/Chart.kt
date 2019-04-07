package lol.adel.graph.data

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
    val colors: SimpleArrayMap<LineId, ColorString>
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
    types.filterKeys { _, type -> type == ColumnType.line }

fun Chart.xs(): LongArray =
    columns[types.findKey { _, type -> type == ColumnType.x }]

inline fun absolutes(chart: Chart, enabled: List<LineId>, result: (Long, Long) -> Unit) {
    var min = Long.MAX_VALUE
    var max = Long.MIN_VALUE

    enabled.forEachByIndex { id ->
        val points = chart[id]
        chart.forEachIndex {
            val p = points[it]
            min = Math.min(min, p)
            max = Math.max(max, p)
        }
    }

    result(min, max)
}

inline fun minMax(chart: Chart, lines: List<LineId>, cameraX: MinMax, result: (Long, Long) -> Unit) {
    var minY = Long.MAX_VALUE
    var maxY = Long.MIN_VALUE

    val minX = 0
    val maxX = chart.size() - 1

    val start = cameraX.min
    val end = cameraX.max

    val begin = clamp(start.ceil(), minX, maxX)
    val finish = clamp(end.floor(), minX, maxX)

    lines.forEachByIndex { id ->
        val points = chart[id]
        for (i in begin..finish) {
            val p = points[i]
            minY = min(minY, p)
            maxY = max(maxY, p)
        }
    }

    result(minY, maxY)
}

fun fillMinMax(chart: Chart, lines: List<LineId>, cameraX: MinMax, result: MinMax) {
    var minY = Long.MAX_VALUE
    var maxY = Long.MIN_VALUE

    val minX = 0
    val maxX = chart.size() - 1

    val start = cameraX.min
    val end = cameraX.max

    val begin = clamp(start.ceil(), minX, maxX)
    val finish = clamp(end.floor(), minX, maxX)

    lines.forEachByIndex { id ->
        val points = chart[id]
        for (i in begin..finish) {
            val p = points[i]
            minY = min(minY, p)
            maxY = max(maxY, p)
        }
    }

    result.set(min = minY.toFloat(), max = maxY.toFloat())
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
