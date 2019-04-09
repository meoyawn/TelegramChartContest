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
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import kotlin.math.abs
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ChartView(
    ctx: Context,
    private val data: Chart,
    private val lineBuf: FloatArray,
    private val cameraX: MinMax,
    private val enabledLines: List<LineId>,
    private val preview: Boolean
) : View(ctx) {

    private companion object {

        // labels
        const val H_LINE_COUNT = 5
        val LINE_LABEL_DIST = 5.dp
        val LINE_PADDING = 16.dpF

        // circles
        val OUTER_CIRCLE_RADIUS = 4.dpF
        val INNER_CIRCLE_RADIUS = 3.dpF
    }

    interface Listener {
        fun onTouch(idx: Idx, x: PxF, maxY: Float)
    }

    var listener: Listener? = null

    private fun makeLinePaint(clr: ColorInt): Paint =
        Paint().apply {
            if (isBar()) {
                style = Paint.Style.FILL
                strokeWidth = 1.pxF
            } else {
                style = Paint.Style.STROKE
                strokeWidth = if (preview) 1.dpF else 2.dpF
                strokeCap = Paint.Cap.ROUND
                isAntiAlias = true
            }
            color = clr
        }

    private val animatedColumns = data.lineIds.toSimpleArrayMap { id ->
        AnimatedColumn(
            points = data.columns[id],
            frac = 1f,
            animator = ValueAnimator(),
            paint = makeLinePaint(data.color(id))
        ).apply {
            animator.addUpdateListener {
                frac = it.animatedFloat()
                invalidate()
            }
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

    private val offsetToSeeBottomCircle: Px = if (preview || isBar()) 0 else 5.dp
    private val offsetToSeeTopLabel: Px = if (preview) 0 else 20.dp

    private fun effectiveHeight(): PxF =
        heightF - offsetToSeeBottomCircle - offsetToSeeTopLabel

    private fun mapY(value: Long, height: PxF): Y =
        (1 - cameraY.norm(value)) * effectiveHeight() + offsetToSeeTopLabel

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
        cameraY.set(data.minMax(cameraX, enabledLines))
        anticipatedY.set(cameraY)

        yLabels += YLabel.create(context).apply {
            YLabel.tune(context, this, isBar())
            animator.interpolator = DecelerateInterpolator()
            animator.addUpdateListener {
                setAlpha(it.animatedFraction)
            }
            set(cameraY)
        }

        data.lineIds.forEachByIndex {
            if (it !in enabledLines) {
                animatedColumns[it]?.frac = 0f
            }
        }
    }

    fun cameraXChanged() {
        animateCameraY()
        invalidate() // x changed
    }

    fun lineSelected(select: List<LineId>, deselect: List<LineId>) {
        deselect.forEachByIndex {
            val column = animatedColumns[it]!!
            column.animator.restartWith(column.frac, 0f)
        }
        select.forEachByIndex {
            val column = animatedColumns[it]!!
            column.animator.restartWith(column.frac, 1f)
        }
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

        if (!preview) {
            val currentYLabel = yLabels.first()
            if (tempY.distanceSq(currentYLabel) > (currentYLabel.len() * 0.1f).sq()) {

                // appear
                currentYLabel.run {
                    set(tempY)
                    animator.restart()
                }

                // prune
                repeat(times = yLabels.size - 3) {
                    YLabel.release(yLabels[1], yLabels)
                }

                // fade
                yLabels += YLabel.obtain(ctx = context, list = yLabels, bar = isBar()).apply {
                    set(anticipatedY)
                    animator.start()
                }
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
        if (preview) return false

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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        val touchingIdx = if (!preview && touchingX in 0f..width) {
            val idx = cameraX.denorm(touchingX / width).roundToInt()
            val mappedX = mapX(idx, width)
            canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)
            idx
        } else -1

        val start = cameraX.min
        val end = cameraX.max

        if (isBar()) {
            var x = cameraX.norm(start - start.floor())
            val bw = width / cameraX.len()
            for (i in start.floor()..end.ceil()) {
                var y: PxF = height
                animatedColumns.forEach { _, column ->
                    if (column.frac > 0) {
                        val bh = cameraY.norm(column[i]) * effectiveHeight()
                        canvas.drawRect(x, y - bh, x + bw, y, column.paint)
                        y -= bh
                    }
                }
                x += bw
            }

            drawYLines(height, canvas, width)
        } else {
            drawYLines(height, canvas, width)

            animatedColumns.forEach { _, column ->
                if (column.frac > 0) {
                    val points = column.points

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

                    column.paint.alphaF = column.frac
                    canvas.drawLines(lineBuf, 0, bufIdx, column.paint)
                }
            }

            if (!preview && touchingIdx != -1) {
                animatedColumns.forEach { _, column ->
                    if (column.frac > 0) {
                        mapped(width, height, column.points, touchingIdx) { x, y ->
                            canvas.drawCircle(x, y, OUTER_CIRCLE_RADIUS, column.paint)
                            canvas.drawCircle(x, y, INNER_CIRCLE_RADIUS, innerCirclePaint)
                        }
                    }
                }
            }
        }

        if (!preview) {
            yLabels.forEachByIndex {
                it.iterate(H_LINE_COUNT, it.labelPaint) { value, paint ->
                    drawLabel(canvas, value, height, paint)
                }
            }
        }
    }

    private fun drawLabel(canvas: Canvas, value: Long, height: PxF, paint: Paint): Unit =
        canvas.drawText(chartValue(value, cameraY.max), LINE_PADDING, mapY(value, height) - LINE_LABEL_DIST, paint)

    private fun drawYLines(height: PxF, canvas: Canvas, width: PxF) {
        if (preview) return

        yLabels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.linePaint) { value, paint ->
                val y = mapY(value, height)
                canvas.drawLine(LINE_PADDING, y, width - LINE_PADDING, y, paint)
            }
        }
    }

    private fun isBar(): Boolean =
        data.types.any { _, type -> type == ColumnType.bar }
}
