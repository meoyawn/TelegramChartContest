package lol.adel.graph.data

import androidx.collection.SimpleArrayMap
import help.*

enum class ColumnType {
    line,
    x,
}

typealias ColumnName = String

data class Chart(
    val columns: SimpleArrayMap<ColumnName, LongArray>,
    val types: SimpleArrayMap<ColumnName, ColumnType>,
    val names: SimpleArrayMap<ColumnName, String>,
    val colors: SimpleArrayMap<ColumnName, ColorString>
)

fun Chart.color(name: ColumnName): ColorInt =
    parseColor(colors[name]!!)

fun Chart.size(): Int =
    columns.first().size

operator fun Chart.get(name: ColumnName): LongArray =
    columns[name]!!

fun Chart.lines(): Set<ColumnName> =
    types.filterKeys { _, type -> type == ColumnType.line }
