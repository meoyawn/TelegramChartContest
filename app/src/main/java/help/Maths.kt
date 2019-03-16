package help

import kotlin.math.max
import kotlin.math.min

typealias X = PxF
typealias Y = PxF

typealias Idx = Int
typealias IdxF = Float

fun Float.floor(): Int =
    Math.floor(toDouble()).toInt()

fun Float.ceil(): Int =
    Math.ceil(toDouble()).toInt()

fun Long.log2(): Double =
    Math.log(toDouble()) / Math.log(2.0)

fun Float.log2(): Float =
    (Math.log(toDouble()) / Math.log(2.0)).toFloat()

fun Int.pow2(): Int =
    Math.pow(2.0, toDouble()).toInt()

fun Double.floor(): Long =
    Math.floor(this).toLong()

fun Double.ceil(): Long =
    Math.ceil(this).toLong()

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

fun denormalize(value: Float, min: Long, max: Long): Float =
    min + (max - min) * value

fun avg(a: Float, b: Float): Float =
    (a + b) / 2

fun constrain(from: Float, to: Float, what: Float): Float =
    max(from, min(to, what))
