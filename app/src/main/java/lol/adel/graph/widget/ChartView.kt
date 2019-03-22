package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ChartView(ctx: Context, val data: Chart, lineIds: Set<LineId>) : View(ctx) {

    private companion object {

        // lines
        const val H_LINES = 5
        val H_LINE_THICKNESS = 2.dpF

        // labels
        val LINE_LABEL_DIST = 5.dp
        val LABEL_TEXT_SIZE = 16.dpF

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    interface Listener {
        fun onTouch(idx: Idx, x: PxF, maxY: Float)
    }

    var listener: Listener? = null

    private val cameraX = MinMax(0f, 0f)
    private val cameraY = MinMax(0f, 0f)

    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val enabledLines: MutableSet<LineId> = mutableSetOf()
    private val linePaints: SimpleArrayMap<LineId, Paint> = simpleArrayMapOf()

    private val path = Path()
    private val smoothScroll = SmoothScroll()

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

    fun toggleNight() {
        animatePaint(oldLabelPaint, currentLabelPaint, R.color.label_text)
        animatePaint(oldLinePaint, currentLinePaint, R.color.divider)
        animatePaint(innerCirclePaint, R.color.background)
        animatePaint(verticalLinePaint, R.color.vertical_line)
    }

    //region Touch Feedback
    private var touching: PxF = -1f
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

    init {
        enabledLines.addAll(lineIds)

        for (id in lineIds) {
            linePaints[id] = makeLinePaint(data.color(id))
        }

        absolutes(data, lineIds) { min, max ->
            absoluteMin = min
            absoluteMax = max
        }
    }

    fun setHorizontalBounds(from: IdxF, to: IdxF) {
        val oldStart = cameraX.min
        val oldEnd = cameraX.max

        cameraX.set(from, to)

        calculateMinMax(startDiff = cameraX.min - oldStart, endDiff = cameraX.max - oldEnd)
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

    private fun calculateMinMaxAnimate(): Unit =
        findMax(cameraX, enabledLines, data) { _, max ->
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

    private fun calculateMinMax(startDiff: PxF, endDiff: PxF) {
        if (enabledLines.isEmpty() || (startDiff == 0f && endDiff == 0f)) return

        findMax(cameraX, enabledLines, data) { currentMaxIdx, maybeCurrentMax ->
            val currentIdx = when {
                maybeCurrentMax.toFloat() >= cameraY.max ->
                    currentMaxIdx.toFloat()

                else ->
                    when (startEnd(
                        startDiff,
                        endDiff,
                        goingUp = smoothScroll.anticipatedMax > cameraY.max
                    )) {
                        StartEnd.START ->
                            cameraX.min

                        StartEnd.END ->
                            cameraX.max
                    }
            }
            val currentMax = max(maybeCurrentMax.toFloat(), cameraY.max)

            findMax(cameraX, enabledLines, data, startDiff, endDiff) { anticipatedIdx, anticipatedMax ->
                if (
                    anticipatedMax != smoothScroll.anticipatedMax
                    || Direction.of(startDiff) != smoothScroll.startDir
                    || Direction.of(endDiff) != smoothScroll.endDir
                ) {
                    if (abs(currentLine.max - anticipatedMax.toFloat()) > currentLine.len() / H_LINES) {
                        oldLine.set(from = currentLine)
                        currentLine.min = absoluteMin.toFloat()
                        currentLine.max = anticipatedMax.toFloat()
                    }

                    smoothScroll.visible.set(from = cameraX)
                    smoothScroll.anticipated.set(cameraX.min + startDiff, cameraX.max + endDiff)

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
        cameraY.max = smoothScroll.cameraYMax(cameraX)

        updateAlphas()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                touching = event.x

                val idx = cameraX.denormalize(touching / widthF).roundToInt()
                val mappedX = mapX(idx, widthF, cameraX)

                listener?.onTouch(idx = idx, x = mappedX, maxY = cameraY.max)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = -1f
                listener?.onTouch(idx = -1, x = -1f, maxY = cameraY.max)
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        val touchingIdx = if (touching in 0f..width) {
            val idx = cameraX.denormalize(touching / width).roundToInt()

            val mappedX = mapX(idx, width, cameraX)

            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)

            idx
        } else -1

        oldLine.iterate(H_LINES) {
            val y = mapY(it.toLong(), height, cameraY)
            canvas.drawLine(0f, y, width, y, oldLinePaint)
        }
        currentLine.iterate(H_LINES) {
            val y = mapY(it.toLong(), height, cameraY)
            canvas.drawLine(0f, y, width, y, currentLinePaint)
        }

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

        if (touchingIdx != -1) {
            linePaints.forEach { line, paint ->
                if (paint.alpha > 0) {
                    mapped(width, height, data[line], touchingIdx, cameraX, cameraY) { x, y ->
                        canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, paint)
                        canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }
        }

        oldLine.iterate(H_LINES) {
            val value = it.toLong()
            canvas.drawText(
                chartValue(value, cameraY.max),
                0f,
                mapY(value, height, cameraY) - LINE_LABEL_DIST,
                oldLabelPaint
            )
        }
        currentLine.iterate(H_LINES) {
            val value = it.toLong()
            canvas.drawText(
                chartValue(value, cameraY.max),
                0f,
                mapY(value, height, cameraY) - LINE_LABEL_DIST,
                currentLabelPaint
            )
        }
    }
}
