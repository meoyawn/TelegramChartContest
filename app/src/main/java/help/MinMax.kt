package help

data class MinMax(
    var min: Float,
    var max: Float
)

fun MinMax.distanceSq(that: MinMax): Float =
    (that.min - this.min).sq() + (that.max - this.max).sq()

fun MinMax.set(from: MinMax) {
    min = from.min
    max = from.max
}

fun MinMax.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}

fun MinMax.empty(): Boolean =
    (min == 0f && max == 0f) || (min == Float.MAX_VALUE && max == Float.MIN_VALUE)

inline fun MinMax.iterate(steps: Int, f: (Float) -> Unit): Unit =
    if (empty()) Unit
    else iterate(from = min, to = max, step = (max - min) / steps, f = f)

fun MinMax.normalize(value: Long): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.normalize(value: Int): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.denormalize(value: Float): Float =
    denormalize(value = value, min = min, max = max)
