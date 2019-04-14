package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.*
import lol.adel.graph.widget.ChartView
import kotlin.math.round
import kotlin.math.roundToInt

class LineDrawer(override val view: ChartView) : ChartDrawer {

    companion object {
        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    private val innerCirclePaint = makeInnerCirclePaint(view.context)

    private var touchingX: X = -1f
    private var touchingIdx: IdxF = -1f
    private val touchUp = ValueAnimator().apply {
        addUpdateListener {
            val idx = it.animatedFloat()
            touch(idx, view.yAxis.matrix.mapX(idx))
        }
    }

    private val bottomOffset = if (view.preview) 0 else 5.dp

    override fun touch(idx: IdxF, x: X) {
        touchingX = x
        touchingIdx = idx
        view.listener?.onTouch(touchingIdx.roundToInt(), touchingX)
        view.invalidate()
    }

    override fun touchUp() {
        if (touchingIdx < 0) return

        touchUp.restartWith(touchingIdx, round(touchingIdx))
    }

    override fun touchClear() {
        if (touchingIdx < 0) return

        touchingX = -1f
        touchingIdx = -1f
        view.listener?.onTouch(touchingIdx.roundToInt(), touchingX)
        view.invalidate()
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun draw(canvas: Canvas) {
        val width = view.widthF
        val height = view.heightF

        val yAxis = view.yAxis
        val matrix = yAxis.matrix

        matrix.setup(
            cameraX = view.cameraX,
            cameraY = yAxis.camera,
            right = width,
            bottom = height - bottomOffset,
            top = view.topOffset
        )

        if (!view.preview) {
            yAxis.drawLabelLines(canvas, width)

            val x = touchingX
            canvas.drawLine(x, 0f, x, height, view.verticalLinePaint)
        }

        val columns = view.animatedColumns
        val buf = view.lineBuf
        columns.forEach { _, column ->
            if (column.frac > 0) {
                val bufIdx = fillPolyLine(column.points, buf, view.cameraX)

                matrix.mapPoints(buf, 0, buf, 0, bufIdx)

                column.paint.alphaF = column.frac
                canvas.drawLines(buf, 0, bufIdx, column.paint)
            }
        }

        if (!view.preview) {
            if (touchingIdx >= 0f) {
                val x = touchingX
                columns.forEach { _, column ->
                    if (column.frac > 0) {
                        val y = matrix.mapY(interpolate(touchingIdx, column.points))
                        canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, column.paint)
                        canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }

            yAxis.drawLabels(canvas, width)
        }
    }
}
