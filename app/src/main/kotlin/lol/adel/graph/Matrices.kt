package lol.adel.graph

import android.graphics.Matrix
import help.IdxF
import help.PxF
import help.X
import help.Y
import kotlin.math.ceil
import kotlin.math.floor

private val ARR1 = FloatArray(size = 8)
private val ARR2 = FloatArray(size = 8)

fun Matrix.setup(cameraX: MinMax, cameraY: MinMax, right: PxF, bottom: PxF, top: PxF): Boolean =
    setPolyToPoly(
        ARR1.also {
            it[0] = cameraX.min
            it[1] = cameraY.min
            it[2] = cameraX.min
            it[3] = cameraY.max
            it[4] = cameraX.max
            it[5] = cameraY.min
            it[6] = cameraX.max
            it[7] = cameraY.max
        },
        0,
        ARR2.also {
            it[0] = 0f
            it[1] = bottom
            it[2] = 0f
            it[3] = top
            it[4] = right
            it[5] = bottom
            it[6] = right
            it[7] = top
        },
        0,
        4
    )

/**
 * linear interpolation
 */
fun interpolate(x0: Float, y0: Float, x1: Float, y1: Float, x: Float): Float =
    (y0 * (x1 - x) + y1 * (x - x0)) / (x1 - x0)

fun interpolate(i: IdxF, points: LongArray): Float {
    val floor = floor(i)
    val ceil = ceil(i)

    val floorY = points[floor.toInt()].toFloat()
    val ceilY = points[ceil.toInt()].toFloat()

    return when (i) {
        floor ->
            floorY

        ceil ->
            ceilY

        else ->
            interpolate(
                x0 = floor,
                y0 = floorY,
                x1 = ceil,
                y1 = ceilY,
                x = i
            )
    }
}

private val ARR = FloatArray(size = 2)

fun Matrix.mapX(value: Float): X {
    ARR[0] = value
    ARR[1] = 1f
    mapPoints(ARR)
    return ARR[0]
}

fun Matrix.mapY(value: Float): Y {
    ARR[0] = 1f
    ARR[1] = value
    mapPoints(ARR)
    return ARR[1]
}

fun Matrix.mapY(value: Long): Y =
    mapY(value.toFloat())
