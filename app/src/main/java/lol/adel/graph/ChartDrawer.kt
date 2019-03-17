package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import java.util.concurrent.TimeUnit

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    private val cameraY = MinMax(0f, 0f)
    private val cameraTarget = MinMax(0f, 0f)
    private val currentLine = MinMax(0f, 0f)
    private val oldLine = MinMax(0f, 0f)
    private var oldInstant = SystemClock.elapsedRealtime()

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

        val oldMax = cameraY.max
        val oldMin = cameraY.min

        if (!drawLabels) {
            cameraY.min = absoluteMin.toFloat()
            cameraY.max = absoluteMax.toFloat()
        } else {
            camera(
                start = start,
                end = end,
                minY = absoluteMin,
                maxY = absoluteMax,
                enabled = enabledLines,
                chart = data,
                camera = cameraY,
                absolutes = cameraTarget
            )

            val now = SystemClock.elapsedRealtime()
            val timePassed = now > oldInstant + TimeUnit.SECONDS.toMillis(1)
            if (currentLine.empty() || (currentLine.distanceSq(cameraTarget) > currentLine.lenSq() * 0.2f.sq() && timePassed)) {
                oldLine.set(from = currentLine)
                currentLine.set(from = cameraTarget)
                oldInstant = now
            }
        }

        if (animate) {
            animateFloat(oldMin, cameraY.min) {
                cameraY.min = it
                updateAlphas()
                invalidate()
            }.start()

            animateFloat(oldMax, cameraY.max) {
                cameraY.max = it
                updateAlphas()
                invalidate()
            }.start()

            cameraY.set(oldMin, oldMax)
        } else {
            updateAlphas()
        }
    }

    private fun updateAlphas() {
        val dist1 = currentLine.distanceSq(cameraY)
        val dist2 = oldLine.distanceSq(cameraY)
        val frac1 = dist1 / (dist1 + dist2)

        currentLinePaint.alphaF = 1 - frac1
        currentLabelPain.alphaF = 1 - frac1

        oldLinePaint.alphaF = frac1
        oldLabelPaint.alphaF = frac1
    }

    private fun mapX(idx: Idx, width: PxF): X =
        normalize(value = idx.toFloat(), min = start, max = end) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.normalize(value)) * height

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
                val y = mapY(value, height) - 5.dpF
                canvas.drawText(value.toString(), 0f, y, oldLabelPaint)
            }
            currentLine.iterate(5) {
                val value = it.toLong()
                val y = mapY(value, height) - 5.dpF
                canvas.drawText(value.toString(), 0f, y, currentLabelPain)
            }
        }
    }
}
