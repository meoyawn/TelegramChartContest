package help

import android.graphics.Paint
import kotlin.math.abs

data class MinMax(
    var min: Float,
    var max: Float
)

fun MinMax.len(): Float =
    max - min

fun MinMax.distanceOfMax(that: MinMax): Float =
    abs(that.max - this.max)

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

inline fun MinMax.iterate(steps: Int, paint: Paint, f: (Long, Paint) -> Unit): Unit =
    if (empty() || paint.alpha <= 0) Unit
    else {
        val origStepSize = (max - min) / steps
        val newMax = max - origStepSize / 3
        val newStepSize = (newMax - min) / steps
        iterate(from = min, to = newMax, stepSize = newStepSize, f = { f(it.toLong(), paint) })
    }

fun MinMax.normalize(value: Long): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.normalize(value: Int): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.denormalize(value: Float): Float =
    denormalize(value = value, min = min, max = max)
