package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*

class BackgroundChartView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    private var data: Chart = EMPTY_CHART

    private val cameraX = MinMax(0f, 0f)
    private val cameraY = MinMax(0f, 0f)

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linePaints: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val path = Path()

    fun setHorizontalBounds(from: IdxF, to: IdxF) {
        cameraX.set(from, to)
        invalidate()
    }

    private fun animateCameraY(absoluteMin: Long): Unit =
        findMax(cameraX, enabledLines, data) { _, max ->
            animateFloat(cameraY.min, absoluteMin.toFloat()) {
                cameraY.min = it
                invalidate()
            }.start()
            animateFloat(cameraY.max, max.toFloat()) {
                cameraY.max = it
                invalidate()
            }.start()
        }

    fun selectLine(id: LineId, enabled: Boolean) {
        if (enabled) {
            enabledLines += id
        } else {
            enabledLines -= id
        }

        val paint = linePaints[id]!!
        animateInt(from = paint.alpha, to = if (enabled) 255 else 0) {
            paint.alpha = it
            invalidate()
        }.start()

        absolutes(data, enabledLines) { min, _ -> animateCameraY(min) }
    }

    fun setup(chart: Chart, enabled: Set<LineId>) {
        data = chart

        enabledLines.clear()
        linePaints.clear()
        enabled.forEach { line ->
            enabledLines += line
            linePaints[line] = Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = 2.dpF
                color = chart.color(line)
            }
        }

        absolutes(chart, enabled) { min, max ->
            cameraY.set(min.toFloat(), max.toFloat())
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        val start = cameraX.min
        val end = cameraX.max

        linePaints.forEach { line, paint ->
            if (paint.alpha > 0) {
                path.reset()

                val points = data[line]
                mapped(width, height, points, start.floor(), cameraX, cameraY, path::moveTo)
                for (i in start.ceil()..end.ceil()) {
                    mapped(width, height, points, i, cameraX, cameraY, path::lineTo)
                }

                canvas.drawPath(path, paint)
            }
        }
    }
}
