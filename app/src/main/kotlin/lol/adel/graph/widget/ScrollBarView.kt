package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import android.view.MotionEvent
import android.view.View
import help.*
import lol.adel.graph.Dragging
import lol.adel.graph.Handle
import lol.adel.graph.R
import kotlin.math.max

@SuppressLint("ViewConstructor")
class ScrollBarView(ctx: Context, size: Int) : View(ctx) {

    interface Listener {
        fun onBoundsChange(left: Float, right: Float)
    }

    private val pale = Paint().apply {
        color = ctx.color(R.color.scroll_overlay_pale)
    }
    private val bright = Paint().apply {
        color = ctx.color(R.color.scroll_overlay_bright)
    }

    var listener: Listener? = null

    private var left: Float = 0f
    private var right: Float = 100f

    private val dragging: SparseArray<Dragging> = SparseArray()

    private fun around(x: X, view: X): Boolean =
        Math.abs(x - view) <= 24.dp

    private fun set(left: Float, right: Float) {
        this.left = left
        this.right = right
        listener?.onBoundsChange(left = left / width, right = right / width)
        invalidate()
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putFloat("left", left)
            putFloat("right", right)
        }

    override fun onRestoreInstanceState(state: Parcelable?): Unit =
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("super"))
            left = state.getFloat("left")
            right = state.getFloat("right")
        } else {
            super.onRestoreInstanceState(state)
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.multiTouch(
            down = { pointerId, evX, _ ->
                val draggingSize = dragging.size()
                if (draggingSize == 1 && dragging.valueAt(0)?.handle is Handle.Between) {
                    return true
                }

                if (dragging[pointerId] == null && draggingSize < 2) {
                    val handle = when {
                        evX in (left + 12.dp)..(right - 12.dp) && draggingSize == 0 ->
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

                        self = Dragging(handle = it)

                        self
                    }
                    dragging.put(pointerId, d)

                    parent.requestDisallowInterceptTouchEvent(true)
                }
            },
            move = { pointerId, evX, _ ->
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
            },
            up = { pointerId, _, _ ->
                dragging.delete(pointerId)

                parent.requestDisallowInterceptTouchEvent(false)
            }
        )

        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        val lineWidth = 5.dpF
        val lineHeight = 2.dpF
        val halfLineWidth = lineWidth / 2

        canvas.drawRect(0f, 0f, left - halfLineWidth, height, pale)
        canvas.drawRect(right + halfLineWidth, 0f, width, height, pale)

        canvas.drawRect(left - halfLineWidth, 0f, left + halfLineWidth, height, bright)
        canvas.drawRect(left + halfLineWidth, 0f, right - halfLineWidth, lineHeight, bright)
        canvas.drawRect(left + halfLineWidth, height - lineHeight, right - halfLineWidth, height, bright)
        canvas.drawRect(right - halfLineWidth, 0f, right + halfLineWidth, height, bright)
    }
}
