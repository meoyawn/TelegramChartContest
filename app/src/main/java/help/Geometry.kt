package help

import kotlin.math.sqrt

fun Float.sq(): Float =
    this * this

inline fun yCoordinate(
    radius: Float,
    x: Float,
    x1: Float,
    y1: Float,
    result: (bottom: Float, top: Float) -> Unit
) {
    val cathSq = radius.sq() - (x - x1).sq()
    if (cathSq >= 0) {
        val sqrt = sqrt(cathSq)
        result(y1 - sqrt + radius, y1 + sqrt - radius)
    }
}
