package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import help.*

enum class ColumnType {
    line,
    x,
}

typealias LineId = String

data class Chart(
    val columns: SimpleArrayMap<LineId, LongArray>,
    val types: SimpleArrayMap<LineId, ColumnType>,
    val names: SimpleArrayMap<LineId, String>,
    val colors: SimpleArrayMap<LineId, ColorString>
)

val EMPTY_CHART = Chart(
    columns = simpleArrayMapOf(),
    types = simpleArrayMapOf(),
    names = simpleArrayMapOf(),
    colors = simpleArrayMapOf()
)

fun Chart.color(id: LineId): ColorInt =
    parseColor(colors[id] ?: error("color not found for $id"))

fun Chart.size(): Int =
    columns.first().size

operator fun Chart.get(id: LineId): LongArray =
    columns[id] ?: error("data not found for $id")

fun Chart.lines(): Set<LineId> =
    types.filterKeys { _, type -> type == ColumnType.line }

fun Chart.xs(): LongArray =
    columns[types.findKey { _, type -> type == ColumnType.x }] ?: error("x not found $types")

inline fun Chart.max(from: Idx, to: Idx, enabled: Set<LineId>, f: (Idx, Long) -> Unit) {
    var max = Long.MIN_VALUE
    var iMax = -1
    val size = size()

    for (id in enabled) {
        val points = get(id)
        for (i in Math.max(0, from)..Math.min(to, size - 1)) {
            val value = points[i]
            if (value > max) {
                max = value
                iMax = i
            }
        }
    }

    f(iMax, max)
}

inline fun Chart.min(from: Idx, to: Idx, enabled: Set<LineId>, f: (Idx, Long) -> Unit) {
    var min = Long.MAX_VALUE
    var iMin = -1
    val size = size()

    for (id in enabled) {
        val points = get(id)
        for (i in Math.max(0, from)..Math.min(to, size - 1)) {
            val value = points[i]
            if (value < min) {
                min = value
                iMin = i
            }
        }
    }

    f(iMin, min)
}

fun normalize(value: Long, min: Long, max: Long): Float =
    Math.abs((value - min) / (max - min).toFloat())

fun normalize(value: Int, min: Int, max: Int): Float =
    Math.abs((value - min) / (max - min).toFloat())

fun denormalize(value: Float, min: Long, max: Long): Float =
    min + (max - min) * value

inline fun findY(start: Idx, end: Idx, enabled: Set<LineId>, chart: Chart, result: (Float, Float) -> Unit) {
    val r0 = (end - start) / 2
    val x = start + r0
    val r = r0

    var max = Float.MIN_VALUE
    var min = Float.MAX_VALUE

    var minY = Long.MAX_VALUE
    var maxY = Long.MIN_VALUE

    for (id in enabled) {
        val points = chart[id]
        for (i in start..end) {
            minY = Math.min(minY, points[i])
            maxY = Math.max(maxY, points[i])
        }
    }

    val minX = 0
    val maxX = chart.size() - 1

    for (id in enabled) {
        val points = chart[id]
        for (i in start..end) {
            yCoordinate(
                radius = normalize(r, minX, maxX),
                x = normalize(x, minX, maxX),
                x1 = normalize(i, minX, maxX),
                y1 = normalize(points[i], minY, maxY)
            ) { below, above ->
                min = Math.min(min, denormalize(below, minY, maxY))
                max = Math.max(max, denormalize(above, minY, maxY))
            }
        }
    }

    println("$min $max")

    result(min, max)
}
