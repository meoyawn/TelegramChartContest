package help

import android.view.MotionEvent

typealias PointerId = Int

fun MotionEvent.downUpPointerId(): PointerId =
    getPointerId(actionIndex)

fun MotionEvent.downUpX(): Float =
    getX(actionIndex)

fun MotionEvent.downUpY(): Float =
    getY(actionIndex)
