package help

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class MinMax(
    var min: Float,
    var max: Float
)

fun MinMax.size(): Float =
    max - min

fun MinMax.distance(that: MinMax): Float =
    max(
        abs(that.min - this.min),
        abs(that.max - this.max)
    )

fun MinMax.farEnough(that: MinMax): Boolean =
    distance(that) / size() > 0.1

fun MinMax.set(that: MinMax) {
    this.min = that.min
    this.max = that.max
}

fun MinMax.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}

fun MinMax.empty(): Boolean =
    min == 0f && max == 0f

inline fun MinMax.iterate(steps: Int, f: (Float) -> Unit): Unit =
    if (empty()) Unit
    else iterate(from = min, to = max, step = (max - min) / steps, f = f)

fun MinMax.normalize(value: Long): Float =
    normalize(value = value.toFloat(), min = min, max = max)

fun MinMax.reset() {
    min = Float.MAX_VALUE
    max = Float.MIN_VALUE
}

fun MinMax.update(value: Float) {
    min = min(min, value)
    max = max(max, value)
}

fun MinMax.update(bottom: Float, top: Float) {
    min = min(min, bottom)
    max = max(max, top)
}
