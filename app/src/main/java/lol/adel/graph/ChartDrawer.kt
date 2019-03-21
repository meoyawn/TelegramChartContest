package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import kotlin.math.max
import kotlin.math.roundToInt

class ChartDrawer(ctx: Context, val drawLabels: Boolean, val invalidate: () -> Unit) {

    private companion object {

        // lines
        const val H_LINES = 5
        val H_LINE_THICKNESS = 2.dpF

        // labels
        val LINE_LABEL_DIST = 5.dp
        val LABEL_TEXT_SIZE = 16.dpF

        // circles
        val outerCircleRadius = 5.dpF
        val innerCircleRadius = 3.dpF
    }

    private var data: Chart = EMPTY_CHART

    var start: IdxF = 0f
    var end: IdxF = 0f

    val cameraY = MinMax(0f, 0f)
    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linePaints: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    //region Vertical Labels
    private val oldLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = LABEL_TEXT_SIZE
        isAntiAlias = true
    }
    private val currentLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = LABEL_TEXT_SIZE
        isAntiAlias = true
    }
    private val oldLinePaint = Paint().apply {
        color = ctx.color(R.color.divider)
        strokeWidth = H_LINE_THICKNESS
    }
    private val currentLinePaint = Paint().apply {
        color = ctx.color(R.color.divider)
        strokeWidth = H_LINE_THICKNESS
    }
    private val currentLine = MinMax(0f, 0f)
    private val oldLine = MinMax(0f, 0f)
    //endregion

    //region Touch Feedback
    var touching: PxF = -1f
        set(value) {
            field = value
            invalidate()
        }

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
                style = Paint.Style.STROKE
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
        val oldStart = start
        val oldEnd = end

        start = from
        end = to

        calculateMinMax(startDiff = start - oldStart, endDiff = end - oldEnd)
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

        calculateMinMaxAnimate()
    }

    private val smoothScroll = SmoothScroll()

    private fun calculateMinMaxAnimate(): Unit =
        findMax(start, end, enabledLines, data, 0f, 0f) { _, max ->
            oldLine.set(from = currentLine)

            val visibleMax = max.toFloat()
            currentLine.set(absoluteMin.toFloat(), visibleMax)

            animateFloat(cameraY.min, absoluteMin.toFloat()) {
                cameraY.min = it
                updateAlphas()
                invalidate()
            }.start()

            animateFloat(cameraY.max, visibleMax) {
                cameraY.max = it
                updateAlphas()
                invalidate()
            }.start()
        }

    /**
     * depends on [enabledLines], [data], [start], [end]
     */
    private fun calculateMinMax(startDiff: PxF, endDiff: PxF) {
        if (enabledLines.isEmpty() || (startDiff == 0f && endDiff == 0f)) return

        if (!drawLabels) {
            cameraY.min = absoluteMin.toFloat()
            cameraY.max = absoluteMax.toFloat()
        } else {
            findMax(start, end, enabledLines, data) { currentMaxIdx, maybeCurrentMax ->
                val currentIdx = when {
                    maybeCurrentMax.toFloat() >= cameraY.max ->
                        currentMaxIdx.toFloat()

                    else ->
                        when (startEnd(startDiff, endDiff, goingUp = smoothScroll.anticipatedMax > cameraY.max)) {
                            StartEnd.START ->
                                start

                            StartEnd.END ->
                                end
                        }
                }
                val currentMax = max(maybeCurrentMax.toFloat(), cameraY.max)

                findMax(start, end, enabledLines, data, startDiff, endDiff) { anticipatedIdx, anticipatedMax ->
                    if (
                        anticipatedMax != smoothScroll.anticipatedMax
                        || Direction.of(startDiff) != smoothScroll.startDir
                        || Direction.of(endDiff) != smoothScroll.endDir
                    ) {
                        if (anticipatedMax < smoothScroll.anticipatedMax) {
                            oldLine.set(from = currentLine) // down
                        } else {
                            oldLine.set(from = cameraY)
                        }
                        currentLine.min = absoluteMin.toFloat()
                        currentLine.max = anticipatedMax.toFloat()

                        smoothScroll.visibleStart = start
                        smoothScroll.visibleEnd = end

                        smoothScroll.anticipatedStart = start + startDiff
                        smoothScroll.anticipatedEnd = end + endDiff

                        smoothScroll.currentMax = currentMax
                        smoothScroll.currentMaxIdx = currentIdx

                        smoothScroll.anticipatedMax = anticipatedMax
                        smoothScroll.anticipatedMaxIdx = anticipatedIdx

                        smoothScroll.startDir = Direction.of(startDiff)
                        smoothScroll.endDir = Direction.of(endDiff)
                    }

                    if (currentLine.empty()) {
                        currentLine.min = absoluteMin.toFloat()
                        currentLine.max = anticipatedMax.toFloat()
                    }
                }
            }

            cameraY.min = absoluteMin.toFloat()
            cameraY.max = smoothScroll.cameraMax(start, end)

            updateAlphas()
        }
    }

    private fun updateAlphas() {
        val dist1 = currentLine.distanceSq(cameraY)
        val dist2 = oldLine.distanceSq(cameraY)
        val frac1 = dist1 / (dist1 + dist2)

        currentLinePaint.alphaF = 1 - frac1
        currentLabelPaint.alphaF = 1 - frac1

        oldLinePaint.alphaF = frac1
        oldLabelPaint.alphaF = frac1
    }

    fun mapX(idx: Idx, width: PxF): X =
        normalize(value = idx, min = start, max = end) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.normalize(value)) * height

    private val path = Path()

    private inline fun mapped(width: PxF, height: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
        f(
            mapX(idx = idx, width = width),
            mapY(value = points[idx], height = height)
        )

    fun onDraw(canvas: Canvas) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val touchingIdx = if (touching in 0f..width) {
            val idx = denormalize(touching / width, start, end).roundToInt()

            val mappedX = mapX(idx, width)

            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)

            idx
        } else -1

        if (drawLabels) {
            oldLine.iterate(H_LINES) {
                val y = mapY(it.toLong(), height)
                canvas.drawLine(0f, y, width, y, oldLinePaint)
            }
            currentLine.iterate(H_LINES) {
                val y = mapY(it.toLong(), height)
                canvas.drawLine(0f, y, width, y, currentLinePaint)
            }
        }

        linePaints.forEach { line, paint ->
            if (paint.alpha > 0) {
                path.reset()

                val points = data[line]
                mapped(width, height, points, start.floor(), path::moveTo)
                for (i in start.ceil()..end.ceil()) {
                    mapped(width, height, points, i, path::lineTo)
                }

                canvas.drawPath(path, paint)
            }
        }

        if (touchingIdx != -1) {
            linePaints.forEach { line, paint ->
                mapped(width, height, data[line], touchingIdx) { x, y ->
                    canvas.drawCircle(x, y, outerCircleRadius, paint)
                    canvas.drawCircle(x, y, innerCircleRadius, innerCirclePaint)
                }
            }
        }

        if (drawLabels) {
            oldLine.iterate(H_LINES) {
                val value = it.toLong()
                canvas.drawText(
                    chartValue(value, cameraY.max),
                    0f,
                    mapY(value, height) - LINE_LABEL_DIST,
                    oldLabelPaint
                )
            }
            currentLine.iterate(H_LINES) {
                val value = it.toLong()
                canvas.drawText(
                    chartValue(value, cameraY.max),
                    0f,
                    mapY(value, height) - LINE_LABEL_DIST,
                    currentLabelPaint
                )
            }
        }
    }
}
