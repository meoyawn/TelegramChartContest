package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun setHorizontalBounds(from: IdxF, to: IdxF) {
        start = from
        end = to
        recalculateMinMax()
        invalidate()
    }

    var chart: Chart = EMPTY_CHART
        set(value) {
            field = value

            end = value.size().toFloat() - 1

            enabledLines.clear()
            linesForDrawing.clear()
            value.lines().forEach {
                enabledLines += it
                linesForDrawing[it] = Paint().apply {
                    strokeWidth = 2.dpF
                    color = value.color(it)
                }
            }

            recalculateMinMax()
            invalidate()
        }

    private val enabledLines: MutableSet<ColumnName> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<ColumnName, Paint> = simpleArrayMapOf()

    fun enable(name: ColumnName) {
        enabledLines += name

        val paint = linesForDrawing[name]!!
        animateInt(from = paint.alpha, to = 255) {
            paint.alpha = it
            invalidate()
        }.start()

        recalculateMinMax(animate = true)
        invalidate()
    }

    fun disable(name: ColumnName) {
        enabledLines -= name

        val paint = linesForDrawing[name]!!
        animateInt(from = paint.alpha, to = 0) {
            paint.alpha = it
            invalidate()
        }.start()

        recalculateMinMax(animate = true)
        invalidate()
    }

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private var min: Double = 0.0
    private var max: Double = 0.0

    /**
     * depends on [enabledLines], [chart], [start], [end]
     */
    private fun recalculateMinMax(animate: Boolean = false) {
        if (enabledLines.isEmpty()) return

        var visibleMin = Long.MAX_VALUE
        var visibleMax = Long.MIN_VALUE

        val anticipatedStart = start.floor()
        val visibleStart = start.ceil()
        val visibleEnd = end.floor()
        val anticipatedEnd = end.ceil()

        for (line in enabledLines) {
            val points = chart[line]
            for (i in visibleStart..visibleEnd) {
                val point = points[i]
                visibleMax = Math.max(visibleMax, point)
                visibleMin = Math.min(visibleMin, point)
            }
        }

        var anticipatedMax = visibleMax
        var anticipatedMin = visibleMin

        var anticipatedMaxIdx = -1
        var anticipatedMinIdx = -1

        for (line in enabledLines) {
            val points = chart[line]
            val anticipatedLeft = points[anticipatedStart]
            val anticipatedRight = points[anticipatedEnd]

            if (anticipatedLeft > anticipatedMax) {
                anticipatedMax = anticipatedLeft
                anticipatedMaxIdx = anticipatedStart
            }
            if (anticipatedRight > anticipatedMax) {
                anticipatedMax = anticipatedRight
                anticipatedMaxIdx = anticipatedEnd
            }

            if (anticipatedLeft < anticipatedMin) {
                anticipatedMin = anticipatedLeft
                anticipatedMinIdx = anticipatedStart
            }
            if (anticipatedRight < anticipatedMin) {
                anticipatedMin = anticipatedRight
                anticipatedMinIdx = anticipatedEnd
            }
        }

        val maxFraction = when (anticipatedMaxIdx) {
            anticipatedStart ->
                start - anticipatedStart

            anticipatedEnd ->
                anticipatedEnd - end

            else ->
                0f
        }.toDouble()
        val finalMax = visibleMax + Math.abs(anticipatedMax - visibleMax) * (1 - maxFraction)

        val minFraction = when (anticipatedMinIdx) {
            anticipatedStart ->
                start - anticipatedStart

            anticipatedEnd ->
                anticipatedEnd - end

            else ->
                0f
        }.toDouble()
        val finalMin = visibleMin - Math.abs(visibleMin - anticipatedMin) * (1 - minFraction)

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

    fun mapX(idx: Idx): X {
        val width = widthF
        val range = end - start
        val pos = idx - start
        return width / range * pos
    }

    fun mapY(value: Long): Y {
        val height: PxF = heightF
        val range = max - min
        val pos = value - min
        return (height - (height / range * pos)).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        linesForDrawing.forEach { line, paint ->
            if (paint.alpha > 0) {
                val points = chart[line]
                for (i in start.floor()..end.ceil()) {
                    points.getOrNull(index = i + 1)?.let { next ->
                        val point = points[i]
                        canvas.drawLine(mapX(i), mapY(point), mapX(idx = i + 1), mapY(next), paint)
                    }
                }
            }
        }
    }
}
