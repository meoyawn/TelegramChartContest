package help

import kotlin.math.sqrt

fun Float.sq(): Float =
    this * this

inline fun yCoordinate(
    radius: Float,
    x: Float,
    x1: Float,
    y1: Float,
    result: (below: Float, above: Float) -> Unit
) {
    val cathSq = (x - x1).sq()
    val hypoSq = radius.sq()
    if (hypoSq > cathSq) {
        val sqrt = sqrt(hypoSq - cathSq)
        result(y1 - sqrt + radius, y1 + sqrt - radius)
    }
}
