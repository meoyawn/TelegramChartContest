package lol.adel.graph.widget

import android.graphics.Paint
import help.*

fun mapX(idx: Idx, width: PxF, cameraX: MinMax): X =
    cameraX.normalize(idx) * width

fun mapY(value: Long, height: PxF, cameraY: MinMax): Y =
    (1 - cameraY.normalize(value)) * height

inline fun mapped(
    width: PxF,
    height: PxF,
    points: LongArray,
    idx: Idx,
    cameraX: MinMax,
    cameraY: MinMax,
    f: (x: X, y: Y) -> Unit
): Unit =
    f(
        mapX(idx = idx, width = width, cameraX = cameraX),
        mapY(value = points[idx], height = height, cameraY = cameraY)
    )

fun makeLinePaint(clr: ColorInt): Paint =
    Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 2.dpF
        color = clr
    }
