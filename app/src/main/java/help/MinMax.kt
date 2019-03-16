package help

import kotlin.math.abs
import kotlin.math.max

data class MinMax(
    var min: Float,
    var max: Float
)

fun MinMax.range(): Float =
    max - min

fun MinMax.distance(that: MinMax): Float =
    max(abs(that.min - this.min), abs(that.max - this.max))

fun threshold(old: MinMax, new: MinMax): Boolean =
    old.distance(new) / old.range() > 0.1

fun MinMax.set(that: MinMax) {
    this.min = that.min
    this.max = that.max
}

fun MinMax.empty(): Boolean =
    min == 0f && max == 0f

inline fun MinMax.iterate(steps: Int, f: (Float) -> Unit): Unit =
    if (empty()) Unit else iterate(from = min, to = max, step = (max - min) / steps, f = f)
