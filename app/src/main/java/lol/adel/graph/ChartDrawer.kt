package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private val camera = MinMax(0f, 0f)
    private val currentLine = MinMax(0f, 0f)
    private val oldLine = MinMax(0f, 0f)

    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val oldLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }
    private val currentLabelPain = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val oldLinePaint = Paint().apply {
        color = ctx.color(R.color.divider_day)
        strokeWidth = 2.dpF
    }
    private val currentLinePaint = Paint().apply {
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

        calculateCamera(
            start = start,
            end = end,
            minY = absoluteMin,
            maxY = absoluteMax,
            enabled = enabledLines,
            chart = data,
            result = camera
        )

        if (threshold(currentLine, camera)) {
            oldLine.set(currentLine)
            currentLine.set(camera)
        }

        val newFrac = oldLine.distance(camera) / currentLine.distance(camera)
        currentLinePaint.alphaF = newFrac
        currentLabelPain.alphaF = newFrac

        val oldFrac = currentLine.distance(camera) / oldLine.distance(camera)
        oldLinePaint.alphaF = oldFrac
        oldLabelPaint.alphaF = oldFrac
    }

    private fun mapX(idx: Idx, width: PxF): X {
        val range = end - start
        val pos = idx - start
        return width / range * pos
    }

    private fun mapY(value: Long, height: PxF): Y {
        val range = camera.max - camera.min
        val pos = value - camera.min
        return height - (height / range * pos)
    }

    fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        if (drawLabels) {
            oldLine.iterate(5) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, oldLinePaint)
            }
            currentLine.iterate(5) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, currentLinePaint)
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
            oldLine.iterate(5) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, oldLabelPaint)
            }
            currentLine.iterate(5) {
                val value = it.toLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, currentLabelPain)
            }
        }
    }
}
