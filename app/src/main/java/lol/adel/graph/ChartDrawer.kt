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
    private var oldMax: Double = 0.0
    private var oldMin: Double = 0.0
    private var anticipatedMax: Double = 0.0
    private var anticipatedMin: Double = 0.0

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

        var visibleMin = Long.MAX_VALUE
        var visibleMax = Long.MIN_VALUE

        val hiddenStart = start.floor()
        val visibleStart = start.ceil()
        val visibleEnd = end.floor()
        val hiddenEnd = end.ceil()

        for (line in enabledLines) {
            val points = data[line]
            for (i in visibleStart..visibleEnd) {
                val point = points[i]
                visibleMax = Math.max(visibleMax, point)
                visibleMin = Math.min(visibleMin, point)
            }
        }

        var hiddenMax = visibleMax
        var hiddenMin = visibleMin

        var hiddenMaxIdx = -1
        var hiddenMinIdx = -1

        for (line in enabledLines) {
            val points = data[line]
            val anticipatedLeft = points[hiddenStart]
            val anticipatedRight = points[hiddenEnd]

            if (anticipatedLeft > hiddenMax) {
                hiddenMax = anticipatedLeft
                hiddenMaxIdx = hiddenStart
            }
            if (anticipatedRight > hiddenMax) {
                hiddenMax = anticipatedRight
                hiddenMaxIdx = hiddenEnd
            }

            if (anticipatedLeft < hiddenMin) {
                hiddenMin = anticipatedLeft
                hiddenMinIdx = hiddenStart
            }
            if (anticipatedRight < hiddenMin) {
                hiddenMin = anticipatedRight
                hiddenMinIdx = hiddenEnd
            }
        }

        val maxFraction = when (hiddenMaxIdx) {
            hiddenStart ->
                start - hiddenStart

            hiddenEnd ->
                hiddenEnd - end

            else ->
                0f
        }.toDouble()
        val finalMax = visibleMax + Math.abs(hiddenMax - visibleMax) * (1 - maxFraction)

        val minFraction = when (hiddenMinIdx) {
            hiddenStart ->
                start - hiddenStart

            hiddenEnd ->
                hiddenEnd - end

            else ->
                0f
        }.toDouble()
        val finalMin = visibleMin - Math.abs(visibleMin - hiddenMin) * (1 - minFraction)

        oldMin = if (animate) min else visibleMin.toDouble()
        oldMax = if (animate) max else visibleMax.toDouble()

        anticipatedMin = hiddenMin.toDouble()
        anticipatedMax = hiddenMax.toDouble()

        if (animate) {
            animateDouble(min, finalMin) {
                min = it
                invalidate()
            }.start()
            animateDouble(max, finalMax) {
                max = it
                invalidate()
            }.start()
        } else {
            min = finalMin
            max = finalMax
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

            val frac = Math.abs(anticipatedMax - max) / Math.abs(oldMax - max)

            iterate(oldMin, oldMax, (oldMax - oldMin) / 6) {
                val value = it.roundToLong()
                val y = mapY(value, height)
                canvas.drawLine(0f, y, width, y, opaqueLine)
            }

            iterate(anticipatedMin, anticipatedMax, (anticipatedMax - anticipatedMin) / 6) {
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
            iterate(oldMin, oldMax, (oldMax - oldMin) / 6) {
                val value = it.roundToLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, opaque)
            }

            iterate(anticipatedMin, anticipatedMax, (anticipatedMax - anticipatedMin) / 6) {
                val value = it.roundToLong()
                val y = mapY(value, height)
                canvas.drawText(value.toString(), 0f, y, transparent)
            }
        }
    }
}
