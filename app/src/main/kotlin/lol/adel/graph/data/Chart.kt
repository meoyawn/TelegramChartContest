package lol.adel.graph.data

import android.graphics.Color
import androidx.annotation.MainThread
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.reset
import lol.adel.graph.update
import lol.adel.graph.updateMax
import kotlin.math.abs

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

private fun leadingZeros(l: Long): String =
    when (l) {
        in 0..9 ->
            "00$l"

        in 10..100 ->
            "0$l"

        else ->
            l.toString()
    }

fun tooltipValue(v: Long): String =
    when (abs(v)) {
        in 1_000..999_999 ->
            "${v / 1_000} ${leadingZeros(v % 1_000)}"

        in 1_000_000..999_999_999 ->
            "${v / 1_000_000} ${leadingZeros(v % 1_000_000 / 1_000)} ${leadingZeros(v % 1_000)}"

        else ->
            v.toString()
    }
