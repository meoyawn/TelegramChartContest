package lol.adel.graph.data

import android.graphics.Color
import androidx.annotation.MainThread
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.reset
import lol.adel.graph.update
import lol.adel.graph.updateMax
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

fun Columns.first(): LongArray =
    map.first()

operator fun Columns.get(id: LineId?): LongArray =
    map[id] ?: error("no column for $id")

private val PARSE_COLOR: (ColorString) -> ColorInt =
    memoize { Color.parseColor(it) }

fun Chart.color(id: LineId): ColorInt =
    PARSE_COLOR(colors[id] ?: error("color not found for $id"))

operator fun Chart.get(id: LineId): LongArray =
    columns[id]

private val MM = MinMax()

private fun Chart.update(id: LineId, begin: Int, end: Int, local: MinMax) {
    val points = this[id]
    for (i in begin..end) {
        local.update(points[i].toFloat())
    }
}

@MainThread
fun Chart.minMax(cameraX: MinMax, id: LineId): MinMax {
    val minX = 0
    val maxX = size - 1

    val begin = clamp(cameraX.min.ceil(), minX, maxX)
    val end = clamp(cameraX.max.floor(), minX, maxX)

    val local = MM
    local.reset()
    update(id, begin, end, local)
    return local
}

@MainThread
fun Chart.minMax(cameraX: MinMax, lines: List<LineId>): MinMax {

    val minX = 0
    val maxX = size - 1

    val begin = clamp(cameraX.min.ceil(), minX, maxX)
    val end = clamp(cameraX.max.floor(), minX, maxX)

    val local = MM

    local.reset()
    if (type == ChartType.BAR) {
        local.min = 0f
        for (i in begin..end) {
            local.updateMax(lines.sumByIndex { this[it][i] }.toFloat())
        }
    } else {
        lines.forEachByIndex { id ->
            update(id, begin, end, local)
        }
    }

    return local
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

fun chartValue(value: Long): String =
    when (abs(value)) {
        in 0..1_000 ->
            "$value"

        in 1000..1_000_000 ->
            "${rnd(value = value / 1_000.0)}K"

        else ->
            "${rnd(value = value / 1_000_000.0)}M"
    }
