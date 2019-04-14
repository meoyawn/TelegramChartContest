package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.*
import lol.adel.graph.widget.ChartView

class BarDrawer(override val view: ChartView) : ChartDrawer {

    private var touchingFade: Norm = 1f
    private val touchingFadeAnim = ValueAnimator().apply {
        addUpdateListener {
            touchingFade = it.animatedFloat()
            view.invalidate()
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        Paint().apply {
            style = Paint.Style.STROKE
            color = clr
        }

    override fun labelColor(): ColorInt =
        view.color(R.attr.label_text_bars)

    override fun maxLabelAlpha(): Norm =
        0.5f

    override fun draw(canvas: Canvas) {
        val width = view.widthF
        val height = view.heightF

        val yAxis = view.yAxis
        val matrix = yAxis.matrix

        val cameraX = view.cameraX
        val cameraY = yAxis.camera
        matrix.setup(
            cameraX = cameraX,
            cameraY = cameraY,
            right = width,
            bottom = height,
            top = view.topOffset
        )

        val startF = cameraX.min.floor()

        val columns = view.animatedColumns
        val buf = view.lineBuf

        val xRange = cameraX.floorToCeilLen()
        val yRange = columns.size()
        val colorStackSize = xRange * 4

        cameraX.floorToCeil { i ->
            val x = i.toFloat()

            val iOffset = (i - startF) * 4

            var y = 0f
            for (j in 0 until yRange) {
                val column = columns.valueAt(j)
                if (column.frac > 0) {
                    val bufIdx = j * colorStackSize + iOffset
                    val newY = y + column[i]

                    buf[bufIdx + 0] = x
                    buf[bufIdx + 1] = y
                    buf[bufIdx + 2] = x
                    buf[bufIdx + 3] = newY

                    y = newY
                }
            }
        }

        matrix.mapPoints(buf, 0, buf, 0, xRange * yRange * 2)

        val barWidth = width / cameraX.len()
        for (j in 0 until yRange) {
            val column = columns.valueAt(j)
            if (column.frac > 0) {
                column.paint.strokeWidth = barWidth
                canvas.drawLines(buf, j * colorStackSize, colorStackSize, column.paint)
            }
        }

        if (!view.preview) {
            yAxis.drawLabelLines(canvas, width)
            yAxis.drawLabels(canvas, width)
        }
    }
}
