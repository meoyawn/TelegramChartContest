package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ChartView(
    ctx: Context,
    private val data: Chart,
    lineIds: List<LineId>,
    private val lineBuf: FloatArray
) : View(ctx) {

    private companion object {

        // labels
        const val H_LINE_COUNT = 5

        val LINE_LABEL_DIST = 5.dp

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
        val Y_OFFSET_TO_SEE_CIRCLES = 5.dp

        fun makeLinePaint(clr: ColorInt): Paint =
            Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 2.dpF
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
                color = clr
            }
    }

    interface Listener {
        fun onTouch(idx: Idx, x: PxF, maxY: Float)
    }

    var listener: Listener? = null

    private val cameraX = MinMax(0f, 0f)

    val enabledLines = ArrayList<LineId>()
    private val linePaints = SimpleArrayMap<LineId, Paint>()

    //region Camera Y
    private val cameraY = MinMax(0f, 0f)
    private val tempY = MinMax(0f, 0f)
    private val anticipatedY = MinMax(0f, 0f)
    //endregion

    //region Vertical Labels
    private val yLabels = ArrayList<YLabel>()
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

    //region Touch Feedback
    private var touchingX: X = -1f
        set(value) {
            field = value
            invalidate()
        }
    private var touchingY: Y = -1f

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

        lineIds.forEachByIndex { id ->
            linePaints[id] = makeLinePaint(data.color(id))
        }

        yLabels += YLabel.create(ctx).apply {
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                setAlpha(it.animatedFraction)
            }
        }

        val dataSize = data.size()
        cameraX.min = dataSize * 0.75f
        cameraX.max = dataSize - 1f

        fillMinMax(data, enabledLines, cameraX, cameraY)
        anticipatedY.set(cameraY)
        yLabels.first().set(cameraY)
    }

    fun setHorizontalBounds(from: IdxF, to: IdxF) {
        cameraX.set(from, to)
        calculateCameraY()
        invalidate()
    }

    fun selectLine(id: LineId, enabled: Boolean) {
        if (enabled) {
            enabledLines += id
        } else {
            enabledLines -= id
        }

        animateAlpha(linePaints[id]!!, if (enabled) 255 else 0)
        calculateCameraY()
    }

    private val cameraMinAnim = ValueAnimator().apply {
        interpolator = DecelerateInterpolator()

        addUpdateListener {
            cameraY.min = it.animatedFloat()
            invalidate()
        }
    }

    private val cameraMaxAnim = ValueAnimator().apply {
        interpolator = DecelerateInterpolator()

        addUpdateListener {
            cameraY.max = it.animatedFloat()
            invalidate()
        }
    }

    private fun calculateCameraY() {
        if (enabledLines.isEmpty()) return

        fillMinMax(data, enabledLines, cameraX, tempY)

        if (tempY == anticipatedY) return

        cameraMinAnim.restartWith(cameraY.min, tempY.min)
        cameraMaxAnim.restartWith(cameraY.max, tempY.max)

        if (tempY.distanceSq(anticipatedY) > (anticipatedY.len() / 10).sq()) {

            // appear
            yLabels.first().run {
                set(tempY)
                animator.restart()
            }

            // prune
            repeat(times = yLabels.size - 2) {
                YLabel.release(yLabels[1], yLabels)
            }

            // fade
            if (!anticipatedY.empty()) {
                yLabels += YLabel.obtain(context, yLabels).apply {
                    set(anticipatedY)
                    animator.start()
                }
            }
        }

        anticipatedY.set(tempY)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {

                val evX = event.x
                val evY = event.y

                if (touchingX != -1f && abs(touchingX - evX) > abs(touchingY - evY)) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }

                touchingX = evX
                touchingY = evY

                val idx = cameraX.denormalize(value = touchingX / widthF).roundToInt()
                val mappedX = mapX(idx, widthF)

                listener?.onTouch(idx = idx, x = mappedX, maxY = cameraY.max)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchingX = -1f
                touchingY = -1f

                listener?.onTouch(idx = -1, x = -1f, maxY = cameraY.max)

                parent.requestDisallowInterceptTouchEvent(false)
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

        val touchingIdx = if (touchingX in 0f..width) {
            val idx = cameraX.denormalize(touchingX / width).roundToInt()
            val mappedX = mapX(idx, width)
            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)
            idx
        } else -1

        yLabels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.linePaint) { value, paint ->
                drawLine(value, height, canvas, width, paint)
            }
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

        yLabels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.labelPaint) { value, paint ->
                drawLabel(canvas, value, height, paint)
            }
        }
    }
}
