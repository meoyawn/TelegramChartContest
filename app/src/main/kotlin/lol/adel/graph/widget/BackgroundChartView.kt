package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.data.*
import lol.adel.graph.normalize

@SuppressLint("ViewConstructor")
class BackgroundChartView(
    ctx: Context,
    private val data: Chart,
    lineIds: List<LineId>,
    private val lineBuf: FloatArray
) : View(ctx) {

    private companion object {

        fun makeLinePaint(clr: ColorInt): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.dpF
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
                color = clr
            }
    }

    private val cameraX = MinMax(0f, 0f)
    private val cameraY = MinMax(0f, 0f)

    private val enabledLines: MutableList<LineId> = ArrayList()
    private val linePaints: SimpleArrayMap<LineId, Paint> = SimpleArrayMap()

    init {
        enabledLines.addAll(lineIds)

        lineIds.forEachByIndex { id ->
            linePaints[id] = makeLinePaint(data.color(id))
        }

        cameraX.min = 0f
        cameraX.max = data.size() - 1f
        fillMinMax(data, enabledLines, cameraX, cameraY)
    }

    private fun mapX(idx: Idx, width: PxF): X =
        cameraX.normalize(idx) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.normalize(value)) * height

    private inline fun mapped(width: PxF, height: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
        f(
            mapX(idx = idx, width = width),
            mapY(value = points[idx], height = height)
        )

    private fun animateCameraY(absoluteMin: Long): Unit =
        minMax(data, enabledLines, cameraX) { _, max ->
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

        animateAlpha(linePaints[id]!!, if (enabled) 255 else 0)

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
                val points = data[line]

                mapped(width, height, points, start.floor()) { x, y ->
                    lineBuf[0] = x
                    lineBuf[1] = y
                }

                var bufIdx = 2
                for (i in start.ceil()..end.ceil()) {
                    mapped(width, height, points, i) { x, y ->
                        bufIdx = fill(lineBuf, bufIdx, x, y)
                    }
                }
                bufIdx -= 2

                canvas.drawLines(lineBuf, 0, bufIdx, paint)
            }
        }
    }
}
