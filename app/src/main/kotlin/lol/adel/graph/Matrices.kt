package lol.adel.graph

import android.graphics.Matrix

private val ARR1 = FloatArray(8)
private val ARR2 = FloatArray(8)

fun Matrix.set(cameraX: MinMax, cameraY: MinMax, height: Float, width: Float): Boolean =
    setPolyToPoly(
        ARR1.apply {
            this[0] = cameraX.min
            this[1] = cameraY.min
            this[2] = cameraX.min
            this[3] = cameraY.max
            this[4] = cameraX.max
            this[5] = cameraY.min
            this[6] = cameraX.max
            this[7] = cameraY.max
        },
        0,
        ARR2.apply {
            this[0] = 0f
            this[1] = height
            this[2] = 0f
            this[3] = 0f
            this[4] = width
            this[5] = height
            this[6] = width
            this[7] = 0f
        },
        0,
        4
    )

