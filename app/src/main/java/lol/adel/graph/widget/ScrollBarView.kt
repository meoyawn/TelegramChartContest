package lol.adel.graph.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import help.*
import lol.adel.graph.Dragging
import lol.adel.graph.Handle
import lol.adel.graph.R
import kotlin.math.max

class ScrollBarView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    private companion object {
        val accelerate = AccelerateInterpolator()
    }

    interface Listener {
        fun onBoundsChange(left: Float, right: Float)
    }

    private val pale = Paint().apply { color = ctx.color(R.color.scroll_overlay_pale) }
    private val bright = Paint().apply { color = ctx.color(R.color.scroll_overlay_bright) }
    private val touch = Paint().apply { color = ctx.color(R.color.scroll_overlay_touch) }

    var listener: Listener? = null

    private var left: Float = -1f
    private var right: Float = -1f

    private val dragging: SparseArray<Dragging> = SparseArray()
    private val wasDragging: SparseArray<Dragging> = SparseArray()

    private fun around(x: X, view: X): Boolean =
        Math.abs(x - view) <= 24.dp

    private fun set(left: Float, right: Float) {
        this.left = left
        this.right = right
        listener?.onBoundsChange(left = left / width, right = right / width)
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerId = event.downUpPointerId()
                val evX = event.downUpX()

                val draggingSize = dragging.size()
                if (draggingSize == 1 && dragging.valueAt(0)?.handle is Handle.Between) {
                    return true
                }
                if (dragging[pointerId] == null && draggingSize < 2) {
                    val handle = when {
                        evX in (left + 24.dp)..(right - 24.dp) && draggingSize == 0 ->
                            Handle.Between(left = left, right = right, x = evX)

                        around(evX, left) ->
                            Handle.Left

                        around(evX, right) ->
                            Handle.Right

                        else ->
                            null
                    }
                    val d = handle?.let {
                        var self: Dragging? = null

                        self = Dragging(
                            radius = 0f,
                            handle = it,
                            anim = animateFloat(0f, (heightF + 32.dp) / 2) {
                                self?.radius = it
                                invalidate()
                            }.apply {
                                interpolator = accelerate
                                start()
                            }
                        )

                        self
                    }
                    dragging.put(pointerId, d)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    val evX = event.getX(i)

                    when (val handle = dragging[pointerId]?.handle) {
                        Handle.Left ->
                            set(clamp(evX, 0f, right - 48.dp), right)

                        Handle.Right ->
                            set(left, clamp(evX, left + 48.dp, widthF))

                        is Handle.Between -> {
                            val diff = evX - handle.x
                            val newLeft = handle.left + diff
                            val newRight = handle.right + diff
                            val distance = handle.right - handle.left

                            when {
                                newLeft >= 0 && newRight < width ->
                                    set(newLeft, newRight)

                                newLeft <= 0 ->
                                    set(left = 0f, right = distance)

                                newRight >= widthF ->
                                    set(left = max(0f, width - 1 - distance), right = width - 1f)
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_POINTER_UP -> {
                val pointerId = event.downUpPointerId()

                dragging[pointerId]?.run {
                    anim.cancel()
                    anim = animateFloat(radius, 0f) {
                        radius = it
                        invalidate()
                    }.apply {
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                wasDragging.delete(pointerId)
                            }
                        })
                        start()
                    }
                    wasDragging.put(pointerId, this)
                }
                dragging.delete(pointerId)
            }
        }

        return true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (this.left < 0) {
            val quarter = width / 4f
            set(quarter, quarter * 3)
        } else {
            set(this.left, this.right)
        }
    }

    override fun onSaveInstanceState(): Parcelable =
        Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putFloat("left", left)
            putFloat("right", right)
        }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("super"))
            left = state.getFloat("left")
            right = state.getFloat("right")
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun draw(d: Dragging?, canvas: Canvas, halfHeight: Float): Unit =
        when (d?.handle) {
            Handle.Left ->
                canvas.drawCircle(left, halfHeight, d.radius, touch)

            Handle.Right ->
                canvas.drawCircle(right, halfHeight, d.radius, touch)

            is Handle.Between ->
                canvas.drawCircle((right - left) / 2 + left, halfHeight, d.radius, touch)

            else ->
                Unit
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF
        val halfHeight = height / 2

        val lineWidth = 5.dpF
        val lineHeight = 2.dpF
        val halfLineWidth = lineWidth / 2

        canvas.drawRect(0f, 0f, left - halfLineWidth, height, pale)
        canvas.drawRect(right + halfLineWidth, 0f, width, height, pale)

        canvas.drawRect(left - halfLineWidth, 0f, left + halfLineWidth, height, bright)
        canvas.drawRect(left + halfLineWidth, 0f, right - halfLineWidth, lineHeight, bright)
        canvas.drawRect(left + halfLineWidth, height - lineHeight, right - halfLineWidth, height, bright)
        canvas.drawRect(right - halfLineWidth, 0f, right + halfLineWidth, height, bright)

        dragging.forEach { _, d -> draw(d, canvas, halfHeight) }
        wasDragging.forEach { _, d -> draw(d, canvas, halfHeight) }
    }
}
