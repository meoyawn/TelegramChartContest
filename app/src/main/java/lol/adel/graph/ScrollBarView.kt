package lol.adel.graph

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import help.*

sealed class Dragging {

    object LEFT : Dragging()

    object RIGHT : Dragging()

    data class BETWEEN(
        val left: X,
        val right: X,
        val x: X
    ) : Dragging()
}

class ScrollBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val blue = Paint().apply { color = Color.BLUE }
    val touch = Paint().apply { color = Color.GREEN }

    var listener: (X, X) -> Unit = { _, _ -> }

    var left: Float = 0f
        set(value) {
            field = value
            listener(left / widthF, right / widthF)
            invalidate()
        }

    var right: Float = 0f
        set(value) {
            field = value
            listener(left / widthF, right / widthF)
            invalidate()
        }

    var dragging: Dragging? = null
    var wasDragging: Dragging? = null

    var radius: PxF = 0f
        set(value) {
            field = value
            invalidate()
        }

    var anim: Animator? = null

    fun around(x: X, view: X): Boolean =
        Math.abs(x - view) <= 24.dp

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    around(event.x, left) ->
                        dragging = Dragging.LEFT

                    around(event.x, right) ->
                        dragging = Dragging.RIGHT

                    event.x in left..right ->
                        dragging = Dragging.BETWEEN(left = left, right = right, x = event.x)
                }

                anim?.cancel()
                anim = animateFloat(radius, heightF / 2) {
                    radius = it
                }
                anim?.start()
            }

            MotionEvent.ACTION_MOVE ->
                when (val d = dragging) {
                    Dragging.LEFT ->
                        left = Math.min(Math.max(event.x, 0f), right)

                    Dragging.RIGHT ->
                        right = Math.max(left, Math.min(event.x, widthF - 1))

                    is Dragging.BETWEEN -> {
                        val diff = event.x - d.x
                        val newLeft = d.left + diff
                        val newRight = d.right + diff
                        val distance = d.right - d.left

                        when {
                            newLeft >= 0 && newRight < width -> {
                                left = newLeft
                                right = newRight
                            }

                            newLeft <= 0 -> {
                                left = 0f
                                right = distance
                            }

                            newRight >= widthF -> {
                                left = widthF - 1 - distance
                                right = widthF - 1
                            }
                        }
                    }
                }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                wasDragging = dragging
                dragging = null

                anim?.cancel()
                anim = animateFloat(radius, 0f) { radius = it }
                anim?.start()
            }
        }
        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        this.right = widthF - 1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(left - 4.dp, 0f, left + 4.dp, heightF, blue)
        canvas.drawRect(right - 4.dp, 0f, right + 4.dp, heightF, blue)
        when (dragging ?: wasDragging) {
            Dragging.LEFT ->
                canvas.drawCircle(left, heightF / 2, radius, touch)

            Dragging.RIGHT ->
                canvas.drawCircle(right, heightF / 2, radius, touch)

            is Dragging.BETWEEN ->
                canvas.drawCircle((right - left) / 2 + left, heightF / 2, radius, touch)
        }
    }
}
