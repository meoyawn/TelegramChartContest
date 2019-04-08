package lol.adel.graph.widget

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.data.*
import lol.adel.graph.empty
import lol.adel.graph.norm
import lol.adel.graph.set

@SuppressLint("ViewConstructor")
class PreviewView(
    ctx: Context,
    private val data: Chart,
    private val allLines: List<LineId>,
    private val lineBuf: FloatArray,
    private val enabledLines: List<LineId>
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

    private val cameraX = MinMax(min = 0f, max = data.size() - 1f)
    private val cameraY = MinMax()

    private val linePaints = SimpleArrayMap<LineId, Paint>().apply {
        allLines.forEachByIndex { id ->
            this[id] = makeLinePaint(data.color(id))
        }
    }

    private var cameraAnim: Animator? = null

    private fun mapX(idx: Idx, width: PxF): X =
        cameraX.norm(idx) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.norm(value)) * height

    private inline fun mapped(width: PxF, height: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
        f(
            mapX(idx = idx, width = width),
            mapY(value = points[idx], height = height)
        )

    fun lineSelected(id: LineId, enabled: Boolean) {
        val paint = linePaints[id]!!
        val toAlpha = if (enabled) 255 else 0
        val toY = data.minMax(cameraX, enabledLines)

        cameraAnim?.cancel()
        cameraAnim = playTogether(
            animateInt(paint.alpha, toAlpha) {
                paint.alpha = it
                invalidate()
            },
            animateFloat(cameraY.min, toY.min) {
                cameraY.min = it
                invalidate()
            },
            animateFloat(cameraY.max, toY.max) {
                cameraY.max = it
                invalidate()
            }
        )
        cameraAnim?.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!cameraY.empty()) return

        cameraY.set(data.minMax(cameraX, enabledLines))
        allLines.forEachByIndex {
            if (it !in enabledLines) {
                linePaints[it]?.alpha = 0
            }
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
