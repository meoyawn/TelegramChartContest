package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
    private val allLines: List<LineId>,
    private val lineBuf: FloatArray,
    private val cameraX: MinMax,
    private val enabledLines: List<LineId>
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

    private val linePaints = SimpleArrayMap<LineId, Paint>().apply {
        allLines.forEachByIndex { id ->
            this[id] = makeLinePaint(data.color(id))
        }
    }

    //region Camera Y
    private val cameraY = MinMax(0f, 0f)
    private val anticipatedY = MinMax(0f, 0f)
    //endregion

    //region Vertical Labels
    private val yLabels = ArrayList<YLabel>()
    //endregion

    private fun mapX(idx: Idx, width: PxF): X =
        cameraX.norm(idx) * width

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.norm(value)) * (height - Y_OFFSET_TO_SEE_CIRCLES * 2) + Y_OFFSET_TO_SEE_CIRCLES

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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (yLabels.isNotEmpty()) return

        cameraY.set(data.minMax(cameraX, enabledLines))
        anticipatedY.set(cameraY)

        yLabels += YLabel.create(context).apply {
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                setAlpha(it.animatedFraction)
            }
            set(cameraY)
        }

        allLines.forEachByIndex {
            if (it !in enabledLines) {
                linePaints[it]?.alpha = 0
            }
        }
    }

    fun cameraXChanged() {
        animateCameraY()
        invalidate() // x changed
    }

    fun lineSelected(id: LineId, enabled: Boolean) {
        animateAlpha(linePaints[id]!!, if (enabled) 255 else 0)
        animateCameraY()
    }

    private val cameraMinAnim = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {
            cameraY.min = it.animatedFloat()
            invalidate()
        }
    }

    private val cameraMaxAnim = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {
            cameraY.max = it.animatedFloat()
            invalidate()
        }
    }

    private fun animateCameraY() {
        if (enabledLines.isEmpty()) return

        val tempY = data.minMax(cameraX, enabledLines)

        if (tempY == anticipatedY) return

        cameraMinAnim.restartWith(cameraY.min, tempY.min)
        cameraMaxAnim.restartWith(cameraY.max, tempY.max)

        if (tempY.distanceSq(anticipatedY) > (anticipatedY.len() / 20).sq()) {

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
            yLabels += YLabel.obtain(context, yLabels).apply {
                set(anticipatedY)
                animator.start()
            }
        }

        anticipatedY.set(tempY)
    }

    private val gd = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return super.onSingleTapUp(e)
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            return super.onScroll(e1, e2, distanceX, distanceY)
        }
    })

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gd.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val evX = event.x
                val evY = event.y

                if (touchingX != -1f && event.action == MotionEvent.ACTION_DOWN) {
                    resetTouch()
                } else {
                    if (touchingX != -1f && abs(touchingX - evX) > abs(touchingY - evY)) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }

                    touchingX = evX
                    touchingY = evY

                    val idx = cameraX.denorm(value = touchingX / widthF).roundToInt()
                    val mappedX = mapX(idx, widthF)

                    listener?.onTouch(idx = idx, x = mappedX, maxY = cameraY.max)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun resetTouch() {
        touchingX = -1f
        touchingY = -1f
        listener?.onTouch(idx = -1, x = -1f, maxY = cameraY.max)
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
            val idx = cameraX.denorm(touchingX / width).roundToInt()
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

        if (data.types.any { _, columnType -> columnType == ColumnType.bar }) {
            var x = cameraX.norm(start - start.floor())
            val bw = width / cameraX.len()
            for (i in start.floor()..end.ceil()) {
                var y: PxF = height
                linePaints.forEach { id, paint ->
                    val point = data[id][i]
                    if (point > 0) {
                        val bh = cameraY.norm(point) * height
                        canvas.drawRect(x, y - bh, x + bw, y, paint)
                        y -= bh
                    }
                }
                x += bw
            }
        } else {
            linePaints.forEach { id, paint ->
                if (paint.alpha > 0) {
                    val points = data[id]

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
