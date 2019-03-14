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
