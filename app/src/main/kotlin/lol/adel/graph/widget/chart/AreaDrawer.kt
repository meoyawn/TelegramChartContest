package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.LineId
import lol.adel.graph.widget.ChartView
import kotlin.math.round
import kotlin.math.roundToInt

class AreaDrawer(override val view: ChartView) : ChartDrawer {

    private companion object {

        fun SimpleArrayMap<LineId, AnimatedColumn>.sum(i: Idx): Float {
            var sum = 0f
            forEachValue { column ->
                if (column.frac > 0) {
                    sum += column[i]
                }
            }
            return sum
        }

        fun SimpleArrayMap<LineId, AnimatedColumn>.findPrevIdx(i: Idx): Idx {
            var j = i - 1
            while (j >= 0 && valueAt(j).frac <= 0) {
                j--
            }
            return j
        }
    }

    private var touchingX: X = -1f
    private var touchingIdx: IdxF = -1f
    private val touchUp = ValueAnimator().apply {
        addUpdateListener {
            val idx = it.animatedFloat()
            touch(idx, view.yAxis.matrix.mapX(idx))
        }
    }

    private val path = Path()

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
        val cameraY = yAxis.camera

        matrix.setup(
            cameraX = cameraX,
            cameraY = cameraY,
            right = width,
            bottom = height,
            top = view.topOffset
        )

        val columns = view.animatedColumns
        var lastJ = -1

        val startF = cameraX.min.floor()
        val endC = cameraX.max.ceil()

        val buf = view.lineBuf

        val iSize = cameraX.floorToCeilLen() + 1
        val jSize = columns.size()

        // setup fillers
        run {
            val mult = cameraY.max / columns.sum(startF)
            val x = startF.toFloat()
            var y = 0f
            columns.forEachIndex { j ->
                val column = columns.valueAt(j)
                if (column.frac > 0) {
                    y += column[startF] * mult
                    if (y.roundToInt() == cameraY.max.roundToInt()) {
                        lastJ = j
                    }
                    buf.setPoint(i = 0, j = j, jSize = jSize, x = x, y = y)
                }
            }
        }

        // calc buf
        cameraX.ceilToCeil { i ->
            val mult = cameraY.max / columns.sum(i)

            val x = i.toFloat()

            var y = 0f

            for (j in 0 until lastJ) {
                val column = columns.valueAt(j)

                if (column.frac <= 0) continue

                // START NOT FROM GROUND
                y += column[i] * mult
                buf.setPoint(i = i - startF, j = j, jSize = jSize, x = x, y = y)
            }
        }

        // map buf
        val totalSize = getPointIndex(i = iSize, j = jSize, jSize = jSize)
        buf[totalSize + 0] = cameraX.min
        buf[totalSize + 1] = cameraY.min
        buf[totalSize + 2] = cameraX.max
        buf[totalSize + 3] = cameraY.max

        matrix.mapPoints(buf, 0, buf, 0, totalSize + 4)
        val screenLeft = buf[totalSize + 0]
        val screenBottom = buf[totalSize + 1]
        val screenRight = buf[totalSize + 2]
        val screenTop = buf[totalSize + 3]

        for (j in 0 until columns.size()) {
            val column = columns.valueAt(j)
            if (column.frac <= 0) continue

            path.reset()

            val prevJ = columns.findPrevIdx(j)
            when {
                j == lastJ && j == 0 ->
                    path.addRect(screenLeft, screenTop, screenRight, screenBottom, Path.Direction.CW)

                j == 0 -> {
                    buf.getPoint(i = 0, j = j, jSize = jSize, f = path::moveTo)
                    for (i in 1 until iSize) {
                        buf.getPoint(i = i, j = j, jSize = jSize, f = path::lineTo)
                    }

                    path.lineTo(screenRight, screenBottom)
                    path.lineTo(screenLeft, screenBottom)
                }

                j == lastJ -> {
                    if (prevJ >= 0) {
                        buf.getPoint(i = 0, j = prevJ, jSize = jSize, f = path::moveTo)
                        for (i in 1 until iSize) {
                            buf.getPoint(i = i, j = prevJ, jSize = jSize, f = path::lineTo)
                        }
                    } else {
                        path.moveTo(screenLeft, screenBottom)
                        path.lineTo(screenRight, screenBottom)
                    }

                    path.lineTo(screenRight, screenTop)
                    path.lineTo(screenLeft, screenTop)
                }

                else -> {
                    buf.getPoint(i = 0, j = j, jSize = jSize, f = path::moveTo)
                    for (i in 1 until iSize) {
                        buf.getPoint(i = i, j = j, jSize = jSize, f = path::lineTo)
                    }

                    if (prevJ >= 0) {
                        for (i in iSize - 1 downTo 0) {
                            buf.getPoint(i = i, j = prevJ, jSize = jSize, f = path::lineTo)
                        }
                    } else {
                        path.lineTo(screenRight, screenBottom)
                        path.lineTo(screenLeft, screenBottom)
                    }
                }
            }

            canvas.drawPath(path, column.paint)
        }

        if (!view.preview) {
            yAxis.drawLabelLines(canvas, width)

            val x = touchingX
            canvas.drawLine(x, screenTop, x, screenBottom, view.verticalLinePaint)

            yAxis.drawLabels(canvas, width)
        }
    }
}
