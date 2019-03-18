package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import kotlin.math.roundToInt

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private var data: Chart = EMPTY_CHART

    var start: IdxF = 0f
    var end: IdxF = 0f

    private val cameraY = MinMax(0f, 0f)
    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linePaints: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    //region Vertical Labels
    private val oldLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = 16.dpF
        isAntiAlias = true
    }
    private val currentLabelPain = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = 16.dpF
        isAntiAlias = true
    }
    private val oldLinePaint = Paint().apply {
        color = ctx.color(R.color.divider)
        strokeWidth = 2.dpF
    }
    private val currentLinePaint = Paint().apply {
        color = ctx.color(R.color.divider)
        strokeWidth = 2.dpF
    }
    private val currentLine = MinMax(0f, 0f)
    private val oldLine = MinMax(0f, 0f)
    private val cameraTarget = MinMax(0f, 0f)
    //endregion

    //region Touch Feedback
    var touching: PxF = -1f
        set(value) {
            field = value
            invalidate()
        }
    private val outerCircleRadius = 5.dpF
    private val innerCircleRadius = 3.dpF
    private val innerCirclePaint = Paint().apply {
        style = Paint.Style.FILL
        color = ctx.color(R.color.background)
        isAntiAlias = true
    }
    private val verticalLinePaint = Paint().apply {
        strokeWidth = 1.dpF
        color = ctx.color(R.color.vertical_line)
    }
    //endregion

    fun setup(chart: Chart, enabled: Set<LineId>) {
        data = chart

        enabledLines.clear()
        linePaints.clear()
        enabled.forEach { line ->
            enabledLines += line
            linePaints[line] = Paint().apply {
                isAntiAlias = true
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

            if (currentLine.empty() || currentLine.distanceSq(cameraTarget) > currentLine.lenSq() * 0.15f.sq()) {
                oldLine.set(from = currentLine)
                currentLine.set(from = cameraTarget)
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

    fun mapX(idx: Idx, width: PxF): X =
        normalize(value = idx.toFloat(), min = start, max = end) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.normalize(value)) * height

    fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val touchingIdx = if (touching > 0 && touching <= width) {
            val idx = denormalize(touching / width, start, end).roundToInt()

            val mappedX = mapX(idx, width)

            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)

            idx
        } else -1

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

        linePaints.forEach { line, paint ->
            if (paint.alpha > 0) {
                val points = data[line]
                for (i in hiddenStart..Math.min(visibleEnd, points.lastIndex - 1)) {
                    val startX = mapX(idx = i, width = width)
                    val endX = mapX(idx = i + 1, width = width)
                    val startY = mapY(value = points[i], height = height)
                    val endY = mapY(value = points[i + 1], height = height)
                    canvas.drawLine(startX, startY, endX, endY, paint)
                }
            }
        }

        if (touchingIdx != -1) {
            linePaints.forEach { line, paint ->
                val points = data[line]
                val startX = mapX(idx = touchingIdx, width = width)
                val startY = mapY(value = points[touchingIdx], height = height)
                canvas.drawCircle(startX, startY, outerCircleRadius, paint)
                canvas.drawCircle(startX, startY, innerCircleRadius, innerCirclePaint)
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
