package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.animation.DecelerateInterpolator
import help.*
import lol.adel.graph.lerp
import lol.adel.graph.set
import lol.adel.graph.widget.ChartView
import kotlin.math.roundToInt

class LineDrawer(override val view: ChartView) : ChartDrawer {

    companion object {

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    private val innerCirclePaint = makeInnerCirclePaint(view.context)

    var touchingX: X = -1f

    private val arr = FloatArray(2)

    private val touchingSlideAnim = ValueAnimator().apply {
        interpolator = DecelerateInterpolator(2f)
        addUpdateListener {
            touchingX = it.animatedFloat()
            view.listener?.onTouch(idx(touchingX).roundToInt(), touchingX)
            view.invalidate()
        }
    }

    override fun touched(idx: Idx) {
        if (idx >= 0) {
            touchingSlideAnim.restartWith(touchingX, view.mapX(idx, view.widthF))
        } else {
            touchingX = -1f
            view.listener?.onTouch(idx, touchingX)
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun bottomOffset(): Px =
        if (view.preview) 0 else 5.dp

    private val mx = Matrix()
    private val inv = Matrix()

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX

        val axis = view.yAxis
        val eHeight = axis.effectiveHeight()

        mx.set(view.cameraX, view.yAxis.camera, view.widthF, eHeight.toFloat())
        mx.invert(inv)

        val height = view.heightF

        val width = view.widthF

        view.drawYLines(canvas, width)
        if (!view.preview) {
            canvas.drawLine(touchingX, 0f, touchingX, height, view.verticalLinePaint)
        }

        val buf = view.lineBuf
        view.animatedColumns.forEach { id, column ->
            if (column.frac > 0) {
                val points = column.points

                buf[0] = start.floor().toFloat()
                buf[1] = points[start.floor()].toFloat()

                var bufIdx = 2
                for (i in start.ceil()..end.ceil()) {
                    val x = i.toFloat()
                    val y = points[i].toFloat()

                    buf[bufIdx + 0] = x
                    buf[bufIdx + 1] = y
                    buf[bufIdx + 2] = x
                    buf[bufIdx + 3] = y

                    bufIdx += 4
                }
                bufIdx -= 2

                column.paint.alphaF = column.frac

                mx.mapPoints(buf, 0, buf, 0, bufIdx)
                canvas.drawLines(buf, 0, bufIdx, column.paint)
            }
        }

        if (!view.preview && touchingX > 0) {
            view.animatedColumns.forEach { id, column ->
                if (column.frac > 0) {
                    val i = idx(touchingX)
                    val floor = i.floor()
                    val ceil = i.ceil()
                    val points = column.points

                    arr[0] = i
                    arr[1] = lerp(
                        x0 = floor.toFloat(),
                        y0 = points[floor].toFloat(),
                        x1 = ceil.toFloat(),
                        y1 = points[ceil].toFloat(),
                        x = i
                    )

                    mx.mapPoints(arr)

                    val (x, y) = arr
                    canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, column.paint)
                    canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                }
            }
        }
    }

    private fun idx(x: X): IdxF {
        arr[0] = x
        arr[1] = 1f
        inv.mapPoints(arr)
        return arr.first()
    }
}
