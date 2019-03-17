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

fun camera(
    start: IdxF,
    end: IdxF,
    minY: Long,
    maxY: Long,
    enabled: Set<LineId>,
    chart: Chart,
    camera: MinMax,
    absolutes: MinMax
) {
    val minX = 0f
    val maxX = chart.size() - 1f

    val r0 = (end - start) / 2
    val x = start + r0
    val r = r0 + (maxX - minX) / 10f

    var max = Float.MIN_VALUE
    var min = Float.MAX_VALUE

    absolutes.reset()

    for (id in enabled) {
        val points = chart[id]
        for (i in Math.max(minX, x - r).floor()..Math.min(maxX, x + r).ceil()) {
            yCoordinate(
                radius = normalize(r, minX, maxX),
                x = normalize(x, minX, maxX),
                x1 = normalize(i.toFloat(), minX, maxX),
                y1 = normalize(points[i], minY, maxY)
            ) { bottom, top ->
                min = Math.min(min, bottom)
                max = Math.max(max, top)
            }

            absolutes.update(points[i].toFloat())
        }
    }

    camera.min = denormalize(min, minY, maxY)
    camera.max = denormalize(max, minY, maxY)
}
