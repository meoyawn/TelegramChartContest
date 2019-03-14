package help

import kotlin.math.max
import kotlin.math.min

fun constrain(what: Float, from: Float, to: Float): Float =
    max(from, min(to, what))

fun distSq(x: Float, y: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val A = x - x1
    val B = y - y1
    val C = x2 - x1
    val D = y2 - y1

    val dot = A * C + B * D
    val lenSq = sqr(C) + sqr(D)

    val t = constrain(dot / lenSq, 0f, 1f)

    val xx = x1 + t * C
    val yy = y1 + t * D

    val dx = x - xx
    val dy = y - yy

    return sqr(dx) + sqr(dy)
}

fun yCoordinate(distSq: Float, x: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float =
    TODO()
