package lol.adel.graph

import help.denormalize
import help.normalize
import help.sq

data class MinMax(
    var min: Float,
    var max: Float
)

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

fun MinMax.normalize(value: Long): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.normalize(value: Int): Float =
    normalize(value = value, min = min, max = max)

fun MinMax.denormalize(value: Float): Float =
    denormalize(value = value, min = min, max = max)
