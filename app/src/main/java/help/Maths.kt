package help

typealias X = PxF
typealias Y = PxF

typealias Idx = Int
typealias IdxF = Float

fun Float.floor(): Int =
    Math.floor(toDouble()).toInt()

fun Float.ceil(): Int =
    Math.ceil(toDouble()).toInt()
