package lol.adel.graph

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import help.*

class ScrollBarView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    private val pale = Paint().apply { color = ctx.color(R.color.scroll_overlay_pale) }
    private val bright = Paint().apply { color = ctx.color(R.color.scroll_overlay_bright) }
    private val touch = Paint().apply { color = ctx.color(R.color.scroll_overlay_touch) }

    var listener: (X, X) -> Unit = { _, _ -> }

    private var left: Float = 0f
        set(value) {
            field = value
            listener(left / widthF, right / widthF)
            invalidate()
        }

    private var right: Float = 0f
        set(value) {
            field = value
            listener(left / widthF, right / widthF)
            invalidate()
        }

    private var dragging: Dragging? = null
    private var wasDragging: Dragging? = null

    private var radius: PxF = 0f
        set(value) {
            field = value
            invalidate()
        }

    private var anim: Animator? = null

    private fun around(x: X, view: X): Boolean =
        Math.abs(x - view) <= 24.dp

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                when {
                    around(event.x, left) ->
                        dragging = Dragging.Left

                    around(event.x, right) ->
                        dragging = Dragging.Right

                    event.x in left..right ->
                        dragging = Dragging.Between(left = left, right = right, x = event.x)
                }

                anim?.cancel()
                anim = animateFloat(radius, (heightF + 32.dp) / 2) {
                    radius = it
                }
                anim?.start()
            }

            MotionEvent.ACTION_MOVE ->
                when (val d = dragging) {
                    Dragging.Left ->
                        left = Math.min(Math.max(event.x, 0f), right)

                    Dragging.Right ->
                        right = Math.max(left, Math.min(event.x, widthF - 1))

                    is Dragging.Between -> {
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
                anim = animateFloat(radius, 0f) {
                    radius = it
                }
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

        val width = widthF
        val height = heightF
        val halfHeight = height / 2

        val lineWidth = 8.dpF
        val lineHeight = 2.dpF
        val halfLineWidth = lineWidth / 2

        canvas.drawRect(0f, 0f, left - halfLineWidth, height, pale)
        canvas.drawRect(right + halfLineWidth, 0f, width, height, pale)

        canvas.drawRect(left - halfLineWidth, 0f, left + halfLineWidth, height, bright)
        canvas.drawRect(left + halfLineWidth, 0f, right - halfLineWidth, lineHeight, bright)
        canvas.drawRect(left + halfLineWidth, height - lineHeight, right - halfLineWidth, height, bright)
        canvas.drawRect(right - halfLineWidth, 0f, right + halfLineWidth, height, bright)

        when (dragging ?: wasDragging) {
            Dragging.Left ->
                canvas.drawCircle(left, halfHeight, radius, touch)

            Dragging.Right ->
                canvas.drawCircle(right, halfHeight, radius, touch)

            is Dragging.Between ->
                canvas.drawCircle((right - left) / 2 + left, halfHeight, radius, touch)
        }
    }
}
