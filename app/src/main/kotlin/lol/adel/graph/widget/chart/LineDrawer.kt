package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.widget.ChartView

class LineDrawer(override val view: ChartView) : ChartDrawer {

    companion object {

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    private val innerCirclePaint = makeInnerCirclePaint(view.context)

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX
        val cameraY = view.yCamera
        val height = view.heightF
        val eHeight = view.effectiveHeight()
        val width = view.widthF

        view.drawYLines(height, canvas, width)
        view.drawXLine(canvas, width, height)

        val buf = view.lineBuf

        view.animatedColumns.forEach { id, column ->
            if (column.frac > 0) {
                val points = column.points

                view.mapped(width, height, points, start.floor()) { x, y ->
                    // start of first line
                    buf[0] = x
                    buf[1] = y
                }

                var bufIdx = 2
                for (i in start.ceil()..end.ceil()) {
                    view.mapped(width, height, points, i) { x, y ->
                        buf[bufIdx + 0] = x
                        buf[bufIdx + 1] = y
                        buf[bufIdx + 2] = x
                        buf[bufIdx + 3] = y

                        bufIdx += 4
                    }
                }
                bufIdx -= 2

                column.paint.alphaF = column.frac
                canvas.drawLines(buf, 0, bufIdx, column.paint)
            }
        }

        if (!view.preview && view.touchingIdx != -1) {
            view.animatedColumns.forEach { id, column ->
                if (column.frac > 0) {
                    view.mapped(width, height, column.points, view.touchingIdx) { x, y ->
                        canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, column.paint)
                        canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }
        }
    }
}
