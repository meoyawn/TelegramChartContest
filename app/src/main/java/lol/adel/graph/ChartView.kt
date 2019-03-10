package lol.adel.graph

import android.animation.Animator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import kotlin.math.roundToInt

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

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

    var chart: Chart = EMPTY_CHART
        set(value) {
            field = value

            end = value.size().toFloat() - 1

            linesForMinMax.clear()
            linesForDrawing.clear()
            value.lines().forEach {
                linesForMinMax += it
                linesForDrawing[it] = Paint().apply {
                    strokeWidth = 2.dpF
                    color = value.color(it)
                }
            }

            recalculateMinMax()
            invalidate()
        }

    private val linesForMinMax: MutableSet<ColumnName> = mutableSetOf()
    private val linesForDrawing: SimpleArrayMap<ColumnName, Paint> = simpleArrayMapOf()

    fun enable(name: ColumnName) {
        linesForMinMax += name

        val paint = linesForDrawing[name]!!
        animateInt(from = paint.alpha, to = 255) {
            paint.alpha = it
            invalidate()
        }.start()

        recalculateMinMax()
        invalidate()
    }

    fun disable(name: ColumnName) {
        linesForMinMax -= name

        val paint = linesForDrawing[name]!!
        animateInt(from = paint.alpha, to = 0) {
            paint.alpha = it
            invalidate()
        }.start()

        recalculateMinMax()
        invalidate()
    }

    private var min: Long = 0
    private var max: Long = 0

    var minMaxAnim: Animator? = null

    /**
     * depends on [linesForMinMax], [chart], [start], [end]
     */
    private fun recalculateMinMax() {
        if (linesForMinMax.isEmpty()) return

        var tmpMin = Long.MAX_VALUE
        var tmpMax = Long.MIN_VALUE

        val theStart = start.roundToInt()
        val theEnd = end.roundToInt()

        linesForMinMax.forEach { column ->
            for (i in theStart..theEnd) {
                val point = chart[column][i]
                if (point > tmpMax) {
                    tmpMax = point
                }
                if (point < tmpMin) {
                    tmpMin = point
                }
            }
        }

        if (tmpMax != max || tmpMin != min) {
            minMaxAnim?.cancel()
            minMaxAnim = playTogether(
                animateLong(min, tmpMin) {
                    min = it
                    invalidate()
                },
                animateLong(max, tmpMax) {
                    max = it
                    invalidate()
                }
            )
            minMaxAnim?.start()
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
        linesForDrawing.forEach { line, paint ->
            if (paint.alpha > 0) {
                for (i in Math.max(start.roundToInt() - 1, 0)..end.roundToInt()) {
                    chart[line].getOrNull(index = i + 1)?.let { next ->
                        val point = chart[line][i]
                        canvas.drawLine(mapX(i), mapY(point), mapX(idx = i + 1), mapY(next), paint)
                    }
                }
            }
        }
    }
}
