package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import kotlin.math.roundToLong

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private var min: Double = 0.0
    private var max: Double = 0.0

    private var oldMax: Double = Double.NaN
    private var oldMin: Double = Double.NaN

    private val opaque = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }
    private val transparent = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val opaqueLine = Paint().apply {
        color = ctx.color(R.color.divider_day)
        strokeWidth = 2.dpF
    }
    private val transparentLine = Paint().apply {
        color = ctx.color(R.color.divider_day)
        strokeWidth = 2.dpF
    }

    fun setup(chart: Chart, enabled: Set<LineId>) {
        data = chart

        enabledLines.clear()
        linesForDrawing.clear()
        enabled.forEach { line ->
            enabledLines += line
            linesForDrawing[line] = Paint().apply {
                isAntiAlias = true
                isDither = true
                strokeWidth = 2.dpF
                color = chart.color(line)
            }
        }
    }

    fun setHorizontalBounds(from: IdxF, to: IdxF) {
        start = from
        end = to
        calculateMinMax(animate = false)
        invalidate()
    }

    fun onTouch(start: Boolean) {
        if (start) {
            oldMin = min
            oldMax = max
        } else {
            oldMin = Double.NaN
            oldMax = Double.NaN
        }
        invalidate()
    }

    fun selectLine(id: LineId, enabled: Boolean) {
        if (enabled) {
            enabledLines += id
        } else {
            enabledLines -= id
        }

        val paint = linesForDrawing[id]!!
        animateInt(from = paint.alpha, to = if (enabled) 255 else 0) {
            paint.alpha = it
            invalidate()
        }.start()

        calculateMinMax(animate = true)
        invalidate()
    }

    /**
     * depends on [enabledLines], [data], [start], [end]
     */
    private fun calculateMinMax(animate: Boolean) {
        if (enabledLines.isEmpty()) return

        findY(start = start.floor(), end = end.ceil(), enabled = enabledLines, chart = data) { a, b ->
            min = a.toDouble()
            max = b.toDouble()
        }
    }

    private fun mapX(idx: Idx, width: PxF): X {
        val range = end - start
        val pos = idx - start
        return width / range * pos
    }

    private fun mapY(value: Long, height: PxF): Y {
        val range = max - min
        val pos = value - min
        val ret = height - (height / range * pos)
        return ret.toFloat()
    }

    fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        if (drawLabels) {
            if (oldMin != Double.NaN) {
                iterate(oldMin, oldMax, (oldMax - oldMin) / 6) {
                    val value = it.roundToLong()
                    val y = mapY(value, height)
                    canvas.drawLine(0f, y, width, y, opaqueLine)
                }
            }

            iterate(min, max, (max - min) / 6) {
                val value = it.roundToLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, transparentLine)
            }
        }

        val hiddenStart = start.floor()
        val visibleEnd = end.floor()

        linesForDrawing.forEach { line, paint ->
            if (paint.alpha > 0) {
                val points = data[line]
                for (i in hiddenStart..Math.min(visibleEnd, points.lastIndex - 1)) {
                    canvas.drawLine(
                        mapX(idx = i, width = width),
                        mapY(value = points[i], height = height),
                        mapX(idx = i + 1, width = width),
                        mapY(value = points[i + 1], height = height),
                        paint
                    )
                }
            }
        }

        if (drawLabels) {
            if (oldMin != Double.NaN) {
                iterate(oldMin, oldMax, (oldMax - oldMin) / 6) {
                    val value = it.roundToLong()
                    val y = mapY(value, height)
                    canvas.drawText(value.toString(), 0f, y, opaque)
                }
            }

            iterate(min, max, (max - min) / 6) {
                val value = it.roundToLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, transparent)
            }
        }
    }
}
