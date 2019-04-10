package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.get
import lol.adel.graph.widget.ChartView

class AreaDrawer(val view: ChartView) : TypeDrawer {

    private fun sum(i: Idx): Long {
        var sum = 0L

        view.animatedColumns.forEachValue { column ->
            if (column.frac > 0) {
                sum += column[i]
            }
        }

        return sum
    }

    private fun mult(i: Idx): Float =
        (view.heightF - view.offsetToSeeTopLabel) / sum(i)

    override fun makePaint(clr: ColorInt): Paint =
        Paint().apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            color = clr
        }

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX
        val cameraY = view.cameraY
        val height = view.heightF
        val eHeight = view.effectiveHeight()
        val width = view.widthF

        view.animatedColumns.forEachValue { it.path.reset() }

        val eh = view.effectiveHeight() + view.offsetToSeeTopLabel

        kotlin.run {
            val i = start.floor()
            var y: PxF = eh
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.moveTo(view.mapX(i, width), y)
                    y -= column[i] * mult(i)
                }
            }
        }

        for (i in start.floor()..end.ceil()) {
            var y: PxF = eh
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    y -= column[i] * mult(i)
                    column.path.lineTo(view.mapX(i, width), y)
                }
            }
        }

        kotlin.run {
            val i = end.ceil()
            var y: PxF = eh
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.lineTo(view.mapX(i, width), y)
                    y -= column[i] * mult(i)
                }
            }
        }

        for (i in end.ceil() downTo start.floor()) {
            var y: PxF = eh
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.lineTo(view.mapX(i, width), y)
                    y -= column[i] * mult(i)
                }
            }
        }

        view.animatedColumns.forEachValue { column ->
            if (column.frac > 0) {
                column.path.close()
                canvas.drawPath(column.path, column.paint)
            }
        }

        view.drawYLines(height, canvas, width)
    }
}
