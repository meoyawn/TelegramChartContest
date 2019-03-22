package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*

@SuppressLint("ViewConstructor")
class BackgroundChartView(ctx: Context, val data: Chart, lineIds: Set<LineId>) : View(ctx) {

    private val cameraX = MinMax(0f, 0f)
    private val cameraY = MinMax(0f, 0f)

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linePaints: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val path = Path()

    init {
        enabledLines.addAll(lineIds)

        for (id in lineIds) {
            linePaints[id] = makeLinePaint(data.color(id))
        }

        absolutes(data, lineIds) { min, max ->
            cameraY.set(min.toFloat(), max.toFloat())
        }
    }

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