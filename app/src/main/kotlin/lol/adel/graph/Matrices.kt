package lol.adel.graph

import android.graphics.Matrix
import help.IdxF
import help.PxF
import kotlin.math.ceil
import kotlin.math.floor

private val ARR1 = FloatArray(8)
private val ARR2 = FloatArray(8)

fun Matrix.set(cameraX: MinMax, cameraY: MinMax, width: PxF, height: PxF): Boolean =
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
            it[1] = height
            it[2] = 0f
            it[3] = 0f
            it[4] = width
            it[5] = height
            it[6] = width
            it[7] = 0f
        },
        0,
        4
    )

/**
 * linear interpolation
 */
fun lerp(x0: Float, y0: Float, x1: Float, y1: Float, x: Float): Float =
    (y0 * (x1 - x) + y1 * (x - x0)) / (x1 - x0)

fun lerp(i: IdxF, points: LongArray): Float {
    val floor = floor(i)
    val ceil = ceil(i)

    val floorY = points[floor.toInt()].toFloat()
    val ceilY = points[ceil.toInt()].toFloat()

    return when (i) {
        floor ->
            floorY

        ceil ->
            ceilY

        else -> lerp(
            x0 = floor,
            y0 = floorY,
            x1 = ceil,
            y1 = ceilY,
            x = i
        )
    }
}
