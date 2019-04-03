package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import help.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

data class Chart(
    val columns: SimpleArrayMap<LineId, LongArray>,
    val types: SimpleArrayMap<LineId, ColumnType>,
    val names: SimpleArrayMap<LineId, String>,
    val colors: SimpleArrayMap<LineId, ColorString>
)

fun Chart.color(id: LineId): ColorInt =
    parseColor(colors[id] ?: error("color not found for $id"))

fun Chart.size(): Int =
    columns.first().size

operator fun Chart.get(id: LineId): LongArray =
    columns[id] ?: error("data not found for $id")

fun Chart.lineIds(): Set<LineId> =
    types.filterKeys { _, type -> type == ColumnType.line }

fun Chart.xs(): LongArray =
    columns[types.findKey { _, type -> type == ColumnType.x }] ?: error("x not found $types")

inline fun absolutes(chart: Chart, enabled: Set<LineId>, result: (Long, Long) -> Unit) {
    var min = Long.MAX_VALUE
    var max = Long.MIN_VALUE

    for (id in enabled) {
        val points = chart[id]
        for (i in 0 until chart.size()) {
            val p = points[i]
            min = Math.min(min, p)
            max = Math.max(max, p)
        }
    }

    result(min, max)
}

inline fun <T> findMax(
    cameraX: MinMax,
    enabled: Set<LineId>,
    chart: Chart,
    startDiff: PxF = 0f,
    endDiff: PxF = 0f,
    result: (Idx, Long) -> T
) {
    val minX = 0
    val maxX = chart.size() - 1

    var maxY = Long.MIN_VALUE
    var maxIdx = -1

    val start = cameraX.min
    val end = cameraX.max

    val halfRange = (end - start) / 2
    val startMult = sign(startDiff) * halfRange
    val endMult = sign(endDiff) * halfRange

    val begin = clamp((start + startMult).ceil(), minX, maxX)
    val finish = clamp((end + endMult).floor(), minX, maxX)

    for (id in enabled) {
        val points = chart[id]
        for (i in begin..finish) {
            val point = points[i]
            if (point > maxY) {
                maxY = point
                maxIdx = i
            }
        }
    }

    if (maxIdx != -1) {
        result(maxIdx, maxY)
    }
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
