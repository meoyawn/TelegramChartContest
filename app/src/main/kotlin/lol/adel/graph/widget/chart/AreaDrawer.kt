package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.LineId
import lol.adel.graph.widget.ChartView
import kotlin.math.abs
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

        fun SimpleArrayMap<LineId, AnimatedColumn>.goDown(i: Idx): Idx {
            var j = i - 1
            while (j >= 0 && valueAt(j).frac <= 0) {
                j--
            }
            return j
        }
    }

    private val horses = view.data.lineIds.toSimpleArrayMap { Path() }
    private val reverses = view.data.lineIds.toSimpleArrayMap { Path() }
    private val fillers = view.data.lineIds.toSimpleArrayMap { Path() }
    val flipX = Matrix().apply { setScale(-1f, 1f) }

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

        check(lastJ != -1)

        // calc buf
        cameraX.ceilToCeil { i ->
            val mult = cameraY.max / columns.sum(i)

            val x = i.toFloat()

            var y = 0f

            for (j in 0 until lastJ) {
                val column = columns.valueAt(j)

                // START NOT FROM GROUND
                if (column.frac > 0) {
                    y += column[i] * mult
                    buf.setPoint(i = i - startF, j = j, jSize = jSize, x = x, y = y)
                }
            }
        }

        // map buf
        matrix.mapPoints(buf, 0, buf, 0, getPointIndex(i = iSize, j = jSize, jSize = jSize))

        val screenBottom = matrix.mapY(cameraY.min)
        val screenTop = matrix.mapY(cameraY.max)
        val screenLeft = matrix.mapX(cameraX.min)
        val screenRight = matrix.mapX(cameraX.max)

        flipX.setScale(-1f, 1f, abs(screenRight - screenLeft) / 2f, 1f)

        for (j in 0 until 2) {
            val column = columns.valueAt(j)

            val filler = fillers.valueAt(j)
            val horse = horses.valueAt(j)
            val reverse = reverses.valueAt(j)

            filler.rewind()

            if (column.frac > 0) {
                val lower = columns.goDown(j)
                when (j) {
                    0 -> {
                        // fill current horse
                        fill(buf, j, jSize, horse, reverse, iSize)
                        // move right
                        filler.addPath(horse)
                        // move bottom
                        filler.lineTo(screenRight, screenBottom)
                        // move left
                        filler.lineTo(screenLeft, screenBottom)
                    }

                    lastJ -> {
                        buf.getPoint(i = 0, j = lower, jSize = jSize, f = filler::moveTo) // lower
                        // move right
//                        filler.lineTo(screenRight, screenTop)
//                        // move left
//                        filler.addPath(reverses.valueAt(lower))
                    }

                    else -> {
                        // lower
//                        buf.getPoint(i = 0, j = lower, jSize = jSize, f = filler::moveTo)

                        // fill current horse
                        fill(buf, j, jSize, horse, reverse, iSize)

                        // move right
                        filler.addPath(horse)

                        // move bottom
                        buf.getPoint(i = iSize - 1, j = lower, jSize = jSize, f = filler::lineTo)

                        // move left
                        filler.addPath(horses.valueAt(lower))
                    }
                }

                canvas.drawPath(filler, column.paint)
            }
        }
    }

    private fun fill(
        buf: FloatArray,
        j: Int,
        jSize: Int,
        straight: Path,
        reverse: Path,
        iSize: Int
    ) {
        straight.rewind()
        reverse.rewind()

        buf.getPoint(0, j, jSize) { x, y -> straight.moveTo(x, y) }
        buf.getPoint(iSize - 1, j, jSize) { x, y -> reverse.moveTo(x, y) }

        for (i in 1 until iSize) {
            buf.getPoint(i = i, j = j, jSize = jSize, f = straight::lineTo)
            buf.getPoint(i = iSize - i - 1, j = j, jSize = jSize, f = reverse::lineTo)
        }
    }
}
