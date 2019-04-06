package help

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

typealias Idx = Int
typealias IdxF = Float

fun Float.sq(): Float =
    this * this

fun Float.floor(): Int =
    floor(this).toInt()

fun Float.ceil(): Int =
    ceil(this).toInt()

fun Float.log2(): Float =
    (Math.log(toDouble()) / Math.log(2.0)).toFloat()

fun Int.pow2(): Int =
    Math.pow(2.0, toDouble()).toInt()

inline fun iterate(from: Int, to: Int, step: Int, f: (Int) -> Unit) {
    var idx = from
    while (idx <= to) {
        f(idx)
        idx += step
    }
}

inline fun iterate(from: Float, to: Float, stepSize: Float, f: (Float) -> Unit) {
    var idx = from
    while (idx <= to) {
        f(idx)
        idx += stepSize
    }
}

fun normalize(value: Float, min: Float, max: Float): Float =
    abs((value - min) / (max - min))

fun normalize(value: Int, min: Float, max: Float): Float =
    abs((value - min) / (max - min))

fun normalize(value: Long, min: Float, max: Float): Float =
    abs((value - min) / (max - min))

fun denormalize(value: Float, min: Float, max: Float): Float =
    min + (max - min) * value

fun denormalize(value: Float, min: Int, max: Int): Int =
    (min + (max - min) * value).toInt()

fun clamp(value: Float, min: Float, max: Float): Float =
    when {
        value < min ->
            min

        value > max ->
            max

        else ->
            value
    }

fun clamp(value: Int, min: Int, max: Int): Int =
    when {
        value < min ->
            min

        value > max ->
            max

        else ->
            value
    }
