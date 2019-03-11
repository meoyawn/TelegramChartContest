package lol.adel.graph

import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*

class ChartDrawer(val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private var min: Double = 0.0
    private var max: Double = 0.0

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

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
    }
}
