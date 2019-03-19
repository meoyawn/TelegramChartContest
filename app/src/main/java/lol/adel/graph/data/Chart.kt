package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import help.*
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

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

    var maxNorm = Float.MIN_VALUE
    var minNorm = Float.MAX_VALUE

    absolutes.reset()

    val rNorm = normalize(r, minX, maxX)
    val xNorm = normalize(x, minX, maxX)

    for (id in enabled) {
        val points = chart[id]
        for (i in Math.max(minX, x - r).ceil()..Math.min(maxX, x + r).floor()) {
            yCoordinate(
                radius = rNorm,
                x = xNorm,
                x1 = normalize(i, minX, maxX),
                y1 = normalize(points[i], minY, maxY)
            ) { bottom, top ->
                minNorm = min(minNorm, bottom)
                maxNorm = max(maxNorm, top)
            }

            absolutes.update(points[i].toFloat())
        }
    }

    camera.min = denormalize(minNorm, minY, maxY)
    camera.max = denormalize(maxNorm, minY, maxY)
}

private val FMT = DecimalFormat("0.00")

fun chartValue(l: Long, max: Float): String {
    val abs = abs(l)

    return when {
        abs < 1_000 ->
            l.toString()

        abs < 1_000_000 ->
            if (max < 1_000_000) l.toString()
            else "${l / 1_000}K"

        else ->
            "${FMT.format(l / 1_000_000.0)}M"
    }
}
