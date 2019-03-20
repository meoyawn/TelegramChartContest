package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import help.*
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.sign

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

inline fun anticipatedMax(
    start: IdxF,
    end: IdxF,
    enabled: Set<LineId>,
    chart: Chart,
    startDiff: PxF,
    endDiff: PxF,
    result: (Idx, Long) -> Unit
) {
    val maxX = chart.size() - 1

    var theMax = Long.MIN_VALUE
    var maxIdx = -1

    val startMult = sign(startDiff) * 10
    val endMult = sign(endDiff) * 10

    val begin = clamp((start + startMult).ceil(), 0, maxX)
    val finish = clamp((end + endMult).floor(), 0, maxX)

    for (id in enabled) {
        val points = chart[id]
        for (i in begin..finish) {
            val point = points[i]
            if (point > theMax) {
                theMax = point
                maxIdx = i
            }
        }
    }

    if (maxIdx != -1) {
        result(maxIdx, theMax)
    }
}

fun smooth(
    s1: Float,
    e1: Float,
    m1: Float,
    i1: Idx,
    s2: Float,
    e2: Float,
    m2: Float,
    i2: Idx,
    s: Float,
    e: Float
): Float {
    val i2F = i2.toFloat()

    return when {
        m2 > m1 ->
            when {
                abs(s2 - s1) > abs(e2 - e1) -> {
                    println("start $s in ${i2F..s1}")

                    if (s in i2F..s1) {
                        val norm = normalize(s, i2F, s1)
                        denormalize(value = 1 - norm, min = m1, max = m2)
                    } else {
                        println("defaulting")
                        m2
                    }
                }

                else -> {
                    println("end $e")

                    if (e in e1..i2F) {
                        val norm = normalize(e, e1, i2F)
                        println("norm $norm")
                        denormalize(norm, m1, m2)
                    } else {
                        println("defaulting")
                        m2
                    }
                }
            }

        m2 == m1 ->
            m2

        else -> {
            when {
                abs(s2 - s1) > abs(e2 - e1) ->
                    smooth(
                        s1 = i2F,
                        e1 = e2,
                        m1 = m2,
                        i1 = i2,
                        s2 = s1,
                        e2 = e1,
                        m2 = m1,
                        i2 = i1,
                        s = e,
                        e = s
                    )

                else ->
                    smooth(
                        s1 = s2,
                        e1 = i2F,
                        m1 = m2,
                        i1 = i2,
                        s2 = s1,
                        e2 = e1,
                        m2 = m1,
                        i2 = i1,
                        s = s,
                        e = e
                    )
            }
        }
    }
}

private val FMT = DecimalFormat("0.00")

fun chartName(idx: Idx): String =
    "Chart ${idx + 1}"

fun chartValue(value: Long, max: Float): String {
    val abs = abs(value)

    return when {
        abs < 1_000 ->
            value.toString()

        abs < 1_000_000 ->
            if (max < 1_000_000) value.toString()
            else "${value / 1_000} K"

        else ->
            "${FMT.format(value / 1_000_000.0)} M"
    }
}
