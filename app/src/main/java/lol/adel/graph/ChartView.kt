package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import help.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.ColumnType
import lol.adel.graph.data.get
import lol.adel.graph.data.size
import kotlin.math.roundToInt

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val linePaint = Paint().apply {
        strokeWidth = 1.dpF
    }

    var start: IdxF = 0f
        set(value) {
            field = value
            recalculateMinMax()
            invalidate()
        }

    var end: IdxF = 0f
        set(value) {
            field = value
            recalculateMinMax()
            invalidate()
        }

    var chart: Chart = CHARTS[0]
        set(value) {
            field = value

            end = value.size().toFloat() - 1
            enabled = value.types.filterKeys { _, type -> type == ColumnType.line }

            recalculateMinMax()
            invalidate()
        }

    var enabled: Set<String> = emptySet()
        set(value) {
            field = value
            recalculateMinMax()
            invalidate()
        }

    private var min: Long = 0
    private var max: Long = 0

    /**
     * depends on [enabled], [chart], [start], [end]
     */
    private fun recalculateMinMax() {
        min = Long.MAX_VALUE
        max = Long.MIN_VALUE

        val theStart = start.roundToInt()
        val theEnd = end.roundToInt()

        for (column in enabled) {
            for (i in theStart..theEnd) {
                val point = chart[column][i]

                if (point > max) {
                    max = point
                }
                if (point < min) {
                    min = point
                }
            }
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
        val range: Long = max - min
        val pos: Long = value - min
        return height - (height / range * pos)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (column in enabled) {
            linePaint.color = parseColor(chart.colors[column]!!)

            for (i in Math.max(start.roundToInt() - 1, 0)..end.roundToInt()) {
                chart[column].getOrNull(index = i + 1)?.let { next ->
                    val point = chart[column][i]
                    canvas.drawLine(mapX(i), mapY(point), mapX(idx = i + 1), mapY(next), linePaint)
                }
            }
        }
    }
}
