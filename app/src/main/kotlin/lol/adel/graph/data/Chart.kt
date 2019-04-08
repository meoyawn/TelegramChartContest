package lol.adel.graph.data

import android.graphics.Color
import androidx.annotation.MainThread
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.set
import kotlin.math.*

fun Columns.first(): LongArray =
    map.first()

operator fun Columns.get(id: LineId?): LongArray =
    map[id] ?: error("no column for $id")

private val PARSE_COLOR: (ColorString) -> ColorInt =
    memoize { Color.parseColor(it) }

fun Chart.color(id: LineId): ColorInt =
    PARSE_COLOR(colors[id] ?: error("color not found for $id"))

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

    if (types.any { _, v -> v == ColumnType.bar }) {
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

private fun rnd(value: Double): String {
    val prettyRound = round(value * 10) / 10
    val floor = floor(prettyRound).toLong()
    val point = prettyRound - floor

    return if (point == 0.0) {
        "$floor"
    } else {
        "$floor.${(point * 10).toInt()}"
    }
}

fun chartValue(value: Long, max: Float): String =
    when (abs(value)) {
        in 0..1_000 ->
            "$value"

        in 1000..1_000_000 ->
            "${rnd(value = value / 1_000.0)}K"

        else ->
            "${rnd(value = value / 1_000_000.0)}M"
    }
