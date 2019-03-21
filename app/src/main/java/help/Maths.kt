package help

import kotlin.math.round

typealias X = PxF
typealias Y = PxF

typealias Idx = Int
typealias IdxF = Float

fun Float.sq(): Float =
    this * this

fun Float.floor(): Int =
    Math.floor(toDouble()).toInt()

fun Float.ceil(): Int =
    Math.ceil(toDouble()).toInt()

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

inline fun iterate(from: Float, to: Float, step: Float, f: (Float) -> Unit) {
    var idx = from
    while (idx <= to) {
        f(idx)
        idx += step
    }
}

fun normalize(value: Long, min: Long, max: Long): Float =
    (value - min) / (max - min).toFloat()

fun normalize(value: Int, min: Int, max: Int): Float =
    (value - min) / (max - min).toFloat()

fun normalize(value: Float, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun normalize(value: Int, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun normalize(value: Long, min: Float, max: Float): Float =
    (value - min) / (max - min)

fun denormalize(value: Float, min: Long, max: Long): Float =
    min + (max - min) * value

fun denormalize(value: Float, min: Float, max: Float): Float =
    min + (max - min) * value

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
