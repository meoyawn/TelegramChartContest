package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import kotlin.math.abs

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private var currentMin: Float = 0f
    private var currentMax: Float = 0f

    private var oldMax: Float = 0f
    private var oldMin: Float = 0f
    private var newMin: Float = 0f
    private var newMax: Float = 0f

    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val oldText = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }
    private val newText = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val oldLine = Paint().apply {
        color = ctx.color(R.color.divider_day)
        strokeWidth = 2.dpF
    }
    private val newLine = Paint().apply {
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

        absolutes(chart, enabled) { min, max ->
            absoluteMin = min
            absoluteMax = max
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
            oldMin = currentMin
            oldMax = currentMax
        } else {
            animateInt(oldText.alpha, 0) {
                oldText.alpha = it
                oldLine.alpha = it
                invalidate()
            }.start()
            animateInt(newText.alpha, 255) {
                newText.alpha = it
                newLine.alpha = it
                invalidate()
            }.start()
        }
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

        absolutes(data, enabledLines) { min, max ->
            absoluteMin = min
            absoluteMax = max
        }

        calculateMinMax(animate = true)
        invalidate()
    }

    /**
     * depends on [enabledLines], [data], [start], [end]
     */
    private fun calculateMinMax(animate: Boolean) {
        if (enabledLines.isEmpty()) return

        currentVertical(
            start = start,
            end = end,
            minY = absoluteMin,
            maxY = absoluteMax,
            enabled = enabledLines,
            chart = data
        ) { min, max ->
            currentMin = min
            currentMax = max
        }

        val prevDiff = newMax - newMin
        val currentDiff = currentMax - currentMin
        val frac = currentDiff / prevDiff
        if (frac < 0.9 || frac > 1.1) {
            oldMin = newMin
            oldMax = newMax

            newMin = currentMin
            newMax = currentMax
        } else {
            val old = abs((frac - 1) * 10)
            oldLine.alphaF = old
            newLine.alphaF = 1 - old

            oldText.alphaF = old
            newText.alphaF = 1 - old
        }
    }

    private fun mapX(idx: Idx, width: PxF): X {
        val range = end - start
        val pos = idx - start
        return width / range * pos
    }

    private fun mapY(value: Long, height: PxF): Y {
        val range = currentMax - currentMin
        val pos = value - currentMin
        return height - (height / range * pos)
    }

    fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val oldStep = (oldMax - oldMin).toInt() / 6
        val newStep = (newMax - newMin).toInt() / 6

        if (drawLabels && oldStep > 0 && newStep > 0) {
            iterate(oldMin.toInt(), oldMax.toInt(), oldStep) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, oldLine)
            }

            iterate(newMin.toInt(), newMax.toInt(), newStep) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, newLine)
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

        if (drawLabels && oldStep > 0 && newStep > 0) {
            iterate(oldMin.toInt(), oldMax.toInt(), oldStep) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, oldText)
            }

            iterate(newMin.toInt(), newMax.toInt(), newStep) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, newText)
            }
        }
    }
}
