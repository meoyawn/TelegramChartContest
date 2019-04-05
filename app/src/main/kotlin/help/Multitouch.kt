package help

import android.view.MotionEvent

typealias PxF = Float

typealias X = PxF
typealias Y = PxF

typealias PointerId = Int

inline fun MotionEvent.multiTouch(
    down: (PointerId, X, Y) -> Unit,
    move: (PointerId, X, Y) -> Unit,
    up: (PointerId, X, Y) -> Unit
) {
    val action = actionMasked
    for (i in 0 until pointerCount) {
        val pointer = getPointerId(i)
        val x = getX(i)
        val y = getY(i)

        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN ->
                down(pointer, x, y)

            MotionEvent.ACTION_MOVE ->
                move(pointer, x, y)

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP ->
                up(pointer, x, y)
        }
    }
}
