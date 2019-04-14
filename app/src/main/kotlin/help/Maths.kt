package help

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log

/**
 * 0..1
 */
typealias Norm = Float

typealias Idx = Int
typealias IdxF = Float

fun Float.sq(): Float =
    this * this

fun Float.floor(): Int =
    floor(x = this).toInt()

fun Float.ceil(): Int =
    ceil(x = this).toInt()

fun Float.log2(): Float =
    log(x = this, base = 2f)

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

fun norm(value: Float, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun norm(value: Int, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun norm(value: Float, min: Int, max: Int): Float =
    (value - min) / (max - min)

fun norm(value: Long, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun denorm(value: Float, min: Float, max: Float): Float =
    min + (max - min) * value

fun denorm(value: Float, min: Int, max: Int): Int =
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
