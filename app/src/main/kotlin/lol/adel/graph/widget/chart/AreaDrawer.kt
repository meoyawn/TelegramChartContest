package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.R
import lol.adel.graph.get
import lol.adel.graph.set
import lol.adel.graph.setup
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

//    private fun mult(i: Idx): Float =
//        view.yAxis.effectiveHeight().toFloat() / sum(i)

    override fun initYAxis() {
        val axis = view.yAxis
        axis.camera.set(0f, 100f)
        axis.labels.first().run { set(axis.camera) }
    }

    override fun labelColor(): ColorInt =
        view.color(R.attr.label_text_bars)

    override fun maxLabelAlpha(): Norm =
        0.5f

    override fun verticalSplits(): Int =
        4

    override fun makePaint(clr: ColorInt): Paint =
        Paint().apply {
            style = Paint.Style.FILL
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
            color = clr
        }

    override fun animateYAxis() = Unit

    override fun draw(canvas: Canvas) {
        val width = view.widthF
        val height = view.heightF

        val yAxis = view.yAxis
        val matrix = yAxis.matrix
        val cameraX = view.cameraX

        matrix.setup(
            cameraX = cameraX,
            cameraY = yAxis.camera,
            right = width,
            bottom = height,
            top = view.topOffset
        )

        val columns = view.animatedColumns

        columns.forEachValue { it.path.reset() }

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
            val x = view.mapX(i, width)
            columns.forEachValue { column ->
                if (column.frac > 0) {
                    y -= column[i] * mult(i)
                    column.path.lineTo(x, y)
                }
            }
        }

        run {
            val i = endCeil
            var y: PxF = height
            val x = view.mapX(i, width)

            view.animatedColumns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.lineTo(x, y)
                    y -= column[i] * mult(i)
                }
            }
        }

        for (i in endCeil downTo startFloor) {
            var y: PxF = height
            val x = view.mapX(i, width)
            columns.forEachValue { column ->
                if (column.frac > 0) {
                    column.path.lineTo(x, y)
                    y -= column[i] * mult(i)
                }
            }
        }

        columns.forEachValue { column ->
            if (column.frac > 0) {
                column.path.close()
                canvas.drawPath(column.path, column.paint)
            }
        }

        view.drawYLines(canvas, width)
        view.drawTouchLine(canvas, width, height)
    }
}
