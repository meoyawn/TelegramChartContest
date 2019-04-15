package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.*
import lol.adel.graph.widget.ChartView
import kotlin.math.max
import kotlin.math.roundToInt

class BarDrawer(override val view: ChartView) : ChartDrawer {

    private var touchingIdx: Idx = -1
    private var touchingFade: Norm = 1f
    private val touchingFadeAnim = ValueAnimator().apply {
        addUpdateListener {
            touchingFade = it.animatedFloat()
            view.invalidate()
        }
        onEnd {
            if (touchingFade == 1f) {
                touchingIdx = -1
            }
        }
    }

    override fun touch(idx: IdxF, x: X) {
        touchingIdx = idx.roundToInt()

        if (!touchingFadeAnim.isRunning && touchingFade != 0.5f) {
            touchingFadeAnim.restartWith(touchingFade, 0.5f)
        }

        view.listener?.onTouch(touchingIdx, x)
        view.invalidate()
    }

    override fun touchClear() {
        if (touchingIdx < 0) return

        if (!touchingFadeAnim.isRunning && touchingFade != 1f) {
            view.listener?.onTouch(-1, -1f)
            touchingFadeAnim.restartWith(touchingFade, 1f)
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

        matrix.setup(
            cameraX = cameraX,
            cameraY = yAxis.camera,
            right = width,
            bottom = height,
            top = view.topOffset
        )

        val dataSize = view.data.size
        val startF = clamp(cameraX.min.floor(), 0, dataSize - 1)

        val columns = view.animatedColumns

        val buf = view.lineBuf
        val xRange = cameraX.floorToCeilLen(dataSize) + 1
        val yRange = columns.size()
        val colorStackSize = xRange * 4

        cameraX.floorToCeil(size = dataSize) { i ->
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

        // I don't know why *2, but it works
        matrix.mapPoints(buf, 0, buf, 0, xRange * yRange * 2)

        val barWidth = width / cameraX.len()
        val isTouching = cameraX.contains(touchingIdx, dataSize)
        for (j in 0 until yRange) {
            val column = columns.valueAt(j)
            if (column.frac > 0) {
                column.paint.strokeWidth = barWidth
                column.paint.alphaF = touchingFade

                val start = j * colorStackSize
                if (isTouching) {
                    val preTouchLen = max(0, (touchingIdx - startF) * 4)
                    if (preTouchLen > 0) {
                        canvas.drawLines(buf, start, preTouchLen, column.paint)
                    }

                    val postTouchLen = max(0, colorStackSize - preTouchLen - 4)
                    if (postTouchLen > 0) {
                        canvas.drawLines(buf, start + preTouchLen + 4, postTouchLen, column.paint)
                    }

                    column.paint.alphaF = 1f
                    canvas.drawLines(buf, start + preTouchLen, 4, column.paint)
                } else {
                    canvas.drawLines(buf, start, colorStackSize, column.paint)
                }
            }
        }

        if (!view.preview) {
            yAxis.drawLabelLines(canvas, width)
            yAxis.drawLabels(canvas, width)
        }
    }
}
