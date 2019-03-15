package help

import kotlin.math.sqrt

fun Float.sq(): Float =
    this * this

fun yCoordinate(
    radius: Float,
    x: Float,
    x1: Float,
    y1: Float,
    result: (Float, Float) -> Unit
) {
    val cathSq = (x - x1).sq()
    val hypoSq = radius.sq()
    val sqrt = if (hypoSq > cathSq) sqrt(hypoSq - cathSq) else 0f

    result(y1 - sqrt, y1 + sqrt)
}
