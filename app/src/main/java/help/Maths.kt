package help

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

fun Float.log2(): Double =
    Math.log(toDouble()) / Math.log(2.0)

fun Int.pow2(): Int =
    Math.pow(2.0, toDouble()).toInt()

fun Double.floor(): Long =
    Math.floor(this).toLong()

fun Double.ceil(): Long =
    Math.ceil(this).toLong()
