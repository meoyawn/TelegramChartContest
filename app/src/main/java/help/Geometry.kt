package help

import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun Float.sq(): Float =
    this * this

fun distSq(x1: Float, y1: Float, x2: Float, y2: Float): Float =
    (x2 - x1).sq() + (y2 - y1).sq()

fun crossingY(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float,
    x4: Float,
    y4: Float
): Float =
    ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / ((x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4))

fun constrain(what: Float, from: Float, to: Float): Float =
    max(from, min(to, what))

inline fun lineEquation(x1: Float, y1: Float, x2: Float, y2: Float, f: (Float, Float, Float) -> Unit): Unit =
    f(y1 - y2, x2 - x1, x1 * y2 - x2 * y1)

fun cosSq(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float,
    x4: Float,
    y4: Float
): Float {
    lineEquation(x1, y1, x2, y2) { A1, B1, C1 ->
        lineEquation(x3, y3, x4, y4) { A2, B2, C2 ->
            return (A1 * A2 + B1 * B2).sq() / ((A1.sq() + B1.sq()) * (A2.sq() + B2.sq()))
        }
    }
    error("should never reach")
}

fun dot(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float =
    (x - x1) * (x2 - x1) + (y - y1) * (y2 - y1)

fun yCoordinate(
    radius: Float,
    x: Float,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    result: (Float, Float) -> Unit
) {
    val y3: Float
    val sinSq: Float
    if (x2 == x) {
        y3 = y2
        sinSq = 1 - cosSq(x1 = x1, y1 = y1, x2 = x2, y2 = y2, x3 = x, y3 = 0f, x4 = x, y4 = 1f)
    } else {
        y3 = crossingY(x1 = x1, y1 = y1, x2 = x2, y2 = y2, x3 = x, y3 = 0f, x4 = x, y4 = 1f)
        sinSq = (x2 - x).sq() / distSq(x2, y2, x, y3)
    }

    val hypoSq = radius.sq() / sinSq

    // check if orthogonal

    val hypo = sqrt(hypoSq)
    val cosSq = 1 - sinSq
    val orthLenSq = hypoSq * cosSq
    val orthXDiffSq = orthLenSq * sinSq
    val orthX = x - sqrt(orthXDiffSq)
    when {
        orthX < x1 ->
            TODO("orthx $orthX")

        orthX in x2..x ->
            TODO("orthx $orthX")

        orthX > x ->
            TODO("orthx $orthX")

        else ->
            result(y3 - hypo, y3 + hypo)
    }
}
