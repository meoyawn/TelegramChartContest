package lol.adel.graph

import help.*

data class MinMax(
    var min: Float = 0f,
    var max: Float = 0f
)

operator fun MinMax.contains(i: Int): Boolean =
    i.toFloat() in min..max

inline fun MinMax.floorToCeil(step: Int = 1, size: Int, f: (Int) -> Unit) {
    val lastIdx = size - 1
    var i = clamp(min.floor(), 0, lastIdx)
    val end = clamp(max.ceil(), 0, lastIdx)
    f(i)
    i++
    while (i < end) {
        if (i % step == 0) {
            f(i)
            i += step
        } else {
            i++
        }
    }
    f(end)
}

inline fun MinMax.ceilToCeil(size: Int, f: (Int) -> Unit) {
    val lastIdx = size - 1
    for (i in clamp(min.ceil(), 0, lastIdx)..clamp(max.ceil(), 0, lastIdx)) {
        f(i)
    }
}

fun MinMax.floorToCeilLen(size: Int): Int {
    val lastIdx = size - 1
    return clamp(max.ceil(), 0, lastIdx) - clamp(min.floor(), 0, lastIdx)
}

fun MinMax.contains(x: Int, size: Int): Boolean {
    val lastIdx = size - 1
    val last = clamp(max.ceil(), 0, lastIdx)
    val first = clamp(min.floor(), 0, lastIdx)
    return x in first..last
}

fun MinMax.len(): Float =
    max - min

fun MinMax.set(from: MinMax) {
    this.min = from.min
    this.max = from.max
}

fun MinMax.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}

fun MinMax.reset() {
    min = Float.MAX_VALUE
    max = Float.MIN_VALUE
}

fun MinMax.updateMax(value: Float) {
    max = Math.max(max, value)
}

fun MinMax.update(value: Float) {
    min = Math.min(min, value)
    updateMax(value)
}

fun MinMax.empty(): Boolean =
    (min == 0f && max == 0f) || (min == Float.MAX_VALUE && max == Float.MIN_VALUE)

fun MinMax.norm(value: Long): Float =
    norm(value = value, min = min, max = max)

fun MinMax.norm(value: Int): Float =
    norm(value = value, min = min, max = max)

fun MinMax.norm(value: Float): Float =
    norm(value = value, min = min, max = max)

fun MinMax.denorm(value: Float): Float =
    denorm(value = value, min = min, max = max)
