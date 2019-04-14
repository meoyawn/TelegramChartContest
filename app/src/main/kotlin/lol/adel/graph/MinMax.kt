package lol.adel.graph

import help.ceil
import help.denorm
import help.floor
import help.norm

data class MinMax(
    var min: Float = 0f,
    var max: Float = 0f
)

inline fun MinMax.floorToCeil(f: (Int) -> Unit) {
    for (i in min.floor()..max.ceil()) {
        f(i)
    }
}

inline fun MinMax.ceilToCeil(f: (Int) -> Unit) {
    for (i in min.ceil()..max.ceil()) {
        f(i)
    }
}

fun MinMax.floorToCeilLen(): Int =
    max.ceil() - min.floor()

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
