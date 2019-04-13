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
import kotlin.math.floor
import kotlin.math.roundToInt

class LineDrawer(override val view: ChartView) : ChartDrawer {

    companion object {
        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    private val innerCirclePaint = makeInnerCirclePaint(view.context)

    private val arr = FloatArray(size = 2)

    private var touchingIdx: IdxF = -1f
    private val touchingAnim = ValueAnimator().apply {
        interpolator = DecelerateInterpolator(3f)
        addUpdateListener {
            touchingIdx = it.animatedFloat()
            view.listener?.onTouch(touchingIdx.roundToInt(), map(touchingIdx))
            view.invalidate()
        }
    }

    override fun touched(idx: Idx) {
        if (idx >= 0) {
            touchingAnim.restartWith(touchingIdx, idx.toFloat())
        } else {
            touchingIdx = -1f
            view.listener?.onTouch(-1, -1f)
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun bottomOffset(): Px =
        if (view.preview) 0 else 5.dp

    private val mx = Matrix()

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX

        val axis = view.yAxis
        val eHeight = axis.effectiveHeight()

        mx.set(view.cameraX, view.yAxis.camera, view.widthF, eHeight.toFloat())

        val height = view.heightF

        val width = view.widthF

        view.drawYLines(canvas, width)
        if (!view.preview) {
            val x = map(touchingIdx)
            canvas.drawLine(x, 0f, x, height, view.verticalLinePaint)
        }

        val buf = view.lineBuf
        view.animatedColumns.forEach { id, column ->
            if (column.frac > 0) {
                val points = column.points

                run {
                    val i = floor(start)
                    buf[0] = i
                    buf[1] = points[i.toInt()].toFloat()
                }

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

                mx.mapPoints(buf, 0, buf, 0, bufIdx)
                column.paint.alphaF = column.frac
                canvas.drawLines(buf, 0, bufIdx, column.paint)
            }
        }

        if (!view.preview && touchingIdx >= 0f) {
            view.animatedColumns.forEach { _, column ->
                if (column.frac > 0) {
                    arr[0] = touchingIdx
                    arr[1] = lerp(touchingIdx, column.points)

                    mx.mapPoints(arr)

                    val (x, y) = arr
                    canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, column.paint)
                    canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                }
            }
        }
    }

    private fun map(idx: IdxF): X {
        arr[0] = idx
        arr[1] = 1f
        mx.mapPoints(arr)
        return arr.first()
    }
}
