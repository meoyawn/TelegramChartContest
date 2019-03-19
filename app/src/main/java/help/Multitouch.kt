package help

import android.view.MotionEvent

typealias PointerId = Int

fun MotionEvent.pointerId(): PointerId =
    getPointerId(actionIndex)

fun MotionEvent.x(): Float =
    getX(actionIndex)

fun MotionEvent.y(): Float =
    getY(actionIndex)
