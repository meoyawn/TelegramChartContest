package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.YLabel
import lol.adel.graph.get
import lol.adel.graph.set
import lol.adel.graph.widget.ChartView

class AreaDrawer(override val view: ChartView) : ChartDrawer {

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
        view.yAxis.effectiveHeight().toFloat() / sum(i)

    override fun initYAxis() {
        val ctx = view.context

        val axis = view.yAxis
        axis.camera.set(0f, 100f)
        axis.labels += YLabel.create(ctx).apply {
            YLabel.tune(ctx = ctx, label = this, isBar = false)
            set(axis.camera)
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        Paint().apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            color = clr
        }

    override fun animateYAxis() = Unit

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX
        val height = view.heightF
        val width = view.widthF

        view.animatedColumns.forEachValue { it.path.reset() }

        val startFloor = start.floor()
        val endCeil = end.ceil()

        run {
            val i = startFloor
            var y: PxF = height
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.moveTo(view.mapX(i, width), y)
                    y -= column[i] * mult(i)
                }
            }
        }

        for (i in startFloor..endCeil) {
            var y: PxF = height
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    y -= column[i] * mult(i)
                    column.path.lineTo(view.mapX(i, width), y)
                }
            }
        }

        run {
            val i = endCeil
            var y: PxF = height
            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.lineTo(view.mapX(i, width), y)
                    y -= column[i] * mult(i)
                }
            }
        }

        for (i in endCeil downTo startFloor) {
            var y: PxF = height
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

        view.drawYLines(canvas, width)
        view.drawXLine(canvas, width, height)
    }
}
