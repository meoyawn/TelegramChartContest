package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.collection.ArraySet
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ChartView(
    ctx: Context,
    private val data: Chart,
    lineIds: Set<LineId>,
    private val lineBuf: FloatArray
) : View(ctx) {

    private companion object {

        // labels
        const val H_LINE_COUNT = 5
        val H_LINE_THICKNESS = 2.dpF
        val LINE_LABEL_DIST = 5.dp

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
        val Y_OFFSET_TO_SEE_CIRCLES = 5.dp

        fun makeLinePaint(clr: ColorInt): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                isAntiAlias = true
                strokeWidth = 2.dpF
                color = clr
            }
    }

    interface Listener {
        fun onTouch(idx: Idx, x: PxF, maxY: Float)
    }

    var listener: Listener? = null

    private val cameraX = MinMax(0f, 0f)
    private val cameraY = MinMax(0f, 0f)

    private var absoluteMin: Long = 0
    private var absoluteMax: Long = 0

    private val enabledLines: MutableSet<LineId> = ArraySet()
    private val linePaints: SimpleArrayMap<LineId, Paint> = SimpleArrayMap()

    private val smoothScroll = SmoothScroll()

    //region Vertical Labels
    private val oldLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = HorizontalLabelsView.TEXT_SIZE_PX
        isAntiAlias = true
    }
    private val currentLabelPaint = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = HorizontalLabelsView.TEXT_SIZE_PX
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

    private fun mapX(idx: Idx, width: PxF): X =
        cameraX.normalize(idx) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.normalize(value)) * (height - Y_OFFSET_TO_SEE_CIRCLES * 2) + Y_OFFSET_TO_SEE_CIRCLES

    private inline fun mapped(width: PxF, height: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
        f(
            mapX(idx = idx, width = width),
            mapY(value = points[idx], height = height)
        )

    fun toggleNight() {
        animateColor(oldLabelPaint, currentLabelPaint, R.color.label_text)
        animateColor(oldLinePaint, currentLinePaint, R.color.divider)

        innerCirclePaint.color = color(R.color.background)
        verticalLinePaint.color = color(R.color.vertical_line)
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

        absolutes(data, enabledLines) { min, max ->
            absoluteMin = min
            absoluteMax = max
        }

        animateAlpha(linePaints[id]!!, if (enabled) 255 else 0)
        animateCameraY()
    }

    private fun animateCameraY(): Unit =
        findMax(cameraX, enabledLines, data) { _, max ->
            val visibleMin = absoluteMin.toFloat()
            val visibleMax = max.toFloat()

            oldLine.set(from = currentLine)
            currentLine.set(min = visibleMin, max = visibleMax)

            animateFloat(cameraY.min, visibleMin) {
                cameraY.min = it
                updateLabelAlphas()
                invalidate()
            }.start()

            animateFloat(cameraY.max, visibleMax) {
                cameraY.max = it
                updateLabelAlphas()
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
                    when (startEnd(startDiff, endDiff, goingUp = smoothScroll.anticipatedMax > cameraY.max)) {
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
                    smoothScroll.visible.set(from = cameraX)
                    smoothScroll.anticipated.set(cameraX.min + startDiff, cameraX.max + endDiff)

                    smoothScroll.currentMax = currentMax
                    smoothScroll.currentMaxIdx = currentIdx

                    smoothScroll.anticipatedMax = anticipatedMax
                    smoothScroll.anticipatedMaxIdx = anticipatedIdx

                    smoothScroll.startDir = Direction.of(startDiff)
                    smoothScroll.endDir = Direction.of(endDiff)
                }

                val anticipatedMaxF = anticipatedMax.toFloat()
                if (currentLine.empty() || abs(currentLine.max - anticipatedMaxF) > currentLine.len() / H_LINE_COUNT) {
                    if (currentLine.distanceOfMax(cameraY) < oldLine.distanceOfMax(cameraY)) {
                        oldLine.set(currentLine)
                    }
                    currentLine.min = absoluteMin.toFloat()
                    currentLine.max = anticipatedMaxF
                }
            }
        }

        cameraY.min = absoluteMin.toFloat()
        cameraY.max = smoothScroll.cameraYMax(cameraX)

        updateLabelAlphas()
    }

    fun onTouchStop() {
        val currentDist = currentLine.distanceOfMax(cameraY)
        val oldDist = oldLine.distanceOfMax(cameraY)
        if (currentDist > oldDist) {
            animateAlpha(paint1 = currentLinePaint, paint2 = currentLabelPaint, to = 0)
            animateAlpha(paint1 = oldLinePaint, paint2 = oldLabelPaint, to = 255).onEnd {
                currentLine.set(oldLine)
            }
        } else if (currentDist < oldDist) {
            animateAlpha(paint1 = oldLinePaint, paint2 = oldLabelPaint, to = 0)
            animateAlpha(paint1 = currentLinePaint, paint2 = currentLabelPaint, to = 255).onEnd {
                oldLine.set(currentLine)
            }
        }
    }

    private fun updateLabelAlphas() {
        val dist1 = currentLine.distanceOfMax(cameraY)
        val dist2 = oldLine.distanceOfMax(cameraY)
        val sum = dist1 + dist2

        val oldFrac = when {
            oldLine.empty() ->
                0f

            sum == 0f ->
                0f

            else ->
                dist1 / sum
        }

        oldLinePaint.alphaF = oldFrac
        oldLabelPaint.alphaF = oldFrac

        val currentFrac = 1 - oldFrac
        currentLinePaint.alphaF = currentFrac
        currentLabelPaint.alphaF = currentFrac
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                touching = event.x

                val idx = cameraX.denormalize(touching / widthF).roundToInt()
                val mappedX = mapX(idx, widthF)

                listener?.onTouch(idx = idx, x = mappedX, maxY = cameraY.max)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = -1f
                listener?.onTouch(idx = -1, x = -1f, maxY = cameraY.max)
            }
        }
        return true
    }

    private fun drawLine(value: Long, height: PxF, canvas: Canvas, width: PxF, paint: Paint) {
        val y = mapY(value, height)
        canvas.drawLine(0f, y, width, y, paint)
    }

    private fun drawLabel(canvas: Canvas, value: Long, height: PxF, paint: Paint): Unit =
        canvas.drawText(chartValue(value, cameraY.max), 0f, mapY(value, height) - LINE_LABEL_DIST, paint)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        val touchingIdx = if (touching in 0f..width) {
            val idx = cameraX.denormalize(touching / width).roundToInt()
            val mappedX = mapX(idx, width)
            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)
            idx
        } else -1

        oldLine.iterate(H_LINE_COUNT, oldLinePaint) { value, paint ->
            drawLine(value, height, canvas, width, paint)
        }
        currentLine.iterate(H_LINE_COUNT, currentLinePaint) { value, paint ->
            drawLine(value, height, canvas, width, paint)
        }

        val start = cameraX.min
        val end = cameraX.max

        linePaints.forEach { line, paint ->
            if (paint.alpha > 0) {
                val points = data[line]

                mapped(width, height, points, start.floor()) { x, y ->
                    // start of first line
                    lineBuf[0] = x
                    lineBuf[1] = y
                }

                var iBuf = 2
                for (i in start.ceil()..end.ceil()) {
                    mapped(width, height, points, i) { x, y ->
                        // end of prev line
                        lineBuf[iBuf + 0] = x
                        lineBuf[iBuf + 1] = y

                        // start of next line
                        lineBuf[iBuf + 2] = x
                        lineBuf[iBuf + 3] = y
                    }
                    iBuf += 4
                }
                iBuf -= 2

                canvas.drawLines(lineBuf, 0, iBuf, paint)
            }
        }

        if (touchingIdx != -1) {
            linePaints.forEach { line, paint ->
                if (paint.alpha > 0) {
                    mapped(width, height, data[line], touchingIdx) { x, y ->
                        canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, paint)
                        canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }
        }

        oldLine.iterate(H_LINE_COUNT, oldLabelPaint) { value, paint ->
            drawLabel(canvas, value, height, paint)
        }
        currentLine.iterate(H_LINE_COUNT, currentLabelPaint) { value, paint ->
            drawLabel(canvas, value, height, paint)
        }
    }
}
