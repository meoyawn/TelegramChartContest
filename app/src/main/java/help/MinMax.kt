package help

data class MinMax(
    var min: Float,
    var max: Float
)

operator fun MinMax.compareTo(other: MinMax): Int =
    when {
        this.min == other.min ->
            this.max.compareTo(other.max)

        this.max == other.max ->
            this.min.compareTo(other.min)

        else ->
            0
    }

fun MinMax.len(): Float =
    max - min

fun MinMax.distanceSq(that: MinMax): Float =
    (that.min - this.min).sq() + (that.max - this.max).sq()

fun MinMax.set(from: MinMax) {
    this.min = from.min
    this.max = from.max
}

fun MinMax.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}

fun MinMax.empty(): Boolean =
    (min == 0f && max == 0f) || (min == Float.MAX_VALUE && max == Float.MIN_VALUE)

inline fun MinMax.iterate(steps: Int, f: (Float) -> Unit): Unit =
    if (empty()) Unit
    else {
        val origStepSize = (max - min) / steps
        val newMax = max - origStepSize / 3
        val newStepSize = (newMax - min) / steps
        iterate(from = min, to = newMax, stepSize = newStepSize, f = f)
    }

fun MinMax.normalize(value: Long): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.normalize(value: Int): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.denormalize(value: Float): Float =
    denormalize(value = value, min = min, max = max)
