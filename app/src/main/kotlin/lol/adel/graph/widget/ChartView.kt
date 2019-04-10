package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import lol.adel.graph.widget.chart.*
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ChartView(
    ctx: Context,
    val data: Chart,
    val lineBuf: FloatArray,
    val cameraX: MinMax,
    val enabledLines: List<LineId>,
    val preview: Boolean
) : View(ctx) {

    private companion object {
        // labels
        const val H_LINE_COUNT = 4
        val LINE_LABEL_DIST = 5.dp
        val LINE_PADDING = 16.dpF
    }

    interface Listener {
        fun onTouch(idx: Idx, x: PxF)
    }

    var listener: Listener? = null

    private val drawer: ChartDrawer = when (data.type) {
        ChartType.LINE ->
            LineDrawer(view = this)

        ChartType.TWO_Y ->
            TwoYDrawer(view = this)

        ChartType.BAR ->
            BarDrawer(view = this)

        ChartType.AREA ->
            AreaDrawer(view = this)
    }

    val animatedColumns = data.lineIds.toSimpleArrayMap { id ->
        AnimatedColumn(
            points = data[id],
            animator = ValueAnimator(),
            paint = drawer.makePaint(data.color(id)),
            path = Path()
        ).apply {
            animator.addUpdateListener {
                frac = it.animatedFloat()
                invalidate()
            }
        }
    }

    val yCamera = MinMax(0f, 0f)
    val yAnticipated = MinMax(0f, 0f)
    val yLabels = ArrayList<YLabel>()

    fun mapX(idx: Idx, width: PxF): X =
        cameraX.norm(idx) * width

    private val offsetToSeeBottomCircle: Px = if (preview || !data.line) 0 else 5.dp
    val offsetToSeeTopLabel: Px = if (preview) 0 else 20.dp

    fun effectiveHeight(): PxF =
        heightF - offsetToSeeBottomCircle - offsetToSeeTopLabel

    fun mapY(value: Long): Y =
        (1 - yCamera.norm(value)) * effectiveHeight() + offsetToSeeTopLabel

    inline fun mapped(width: PxF, height: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
        f(
            mapX(idx = idx, width = width),
            mapY(value = points[idx])
        )

    //region Touch Feedback
    var touchingIdx: Idx = -1
    var touchingFade: Norm = 1f
    private val touchingFadeAnim = ValueAnimator().apply {
        addUpdateListener {
            touchingFade = it.animatedFloat()
            invalidate()
        }
        onEnd {
            if (touchingFade == 1f) {
                touchingIdx = -1
            }
        }
    }

    private val verticalLinePaint = Paint().apply {
        strokeWidth = 1.dpF
        color = ctx.color(R.attr.vertical_line)
    }
    //endregion

    init {
        drawer.initYAxis()

        data.lineIds.forEachByIndex {
            if (it !in enabledLines) {
                animatedColumns[it]?.frac = 0f
            }
        }
    }

    fun cameraXChanged() {
        resetTouch()
        drawer.animateYAxis()
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
        drawer.animateYAxis()
    }

    val cameraMinAnim = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {
            yCamera.min = it.animatedFloat()
            invalidate()
        }
    }

    val cameraMaxAnim = ValueAnimator().apply {
        interpolator = AccelerateDecelerateInterpolator()

        addUpdateListener {
            yCamera.max = it.animatedFloat()
            invalidate()
        }
    }

    fun touch(idx: Idx) {
        if (touchingIdx == idx) return

        touchingIdx = idx
        if (isBar()) {
            if (!touchingFadeAnim.isRunning) {
                touchingFadeAnim.restartWith(touchingFade, if (idx == -1) 1f else 0.5f)
            }
        } else {
            invalidate()
        }
    }

    private val gd = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            touch(cameraX.denorm(value = e.x / widthF).roundToInt())
            val mappedX = mapX(touchingIdx, widthF)
            listener?.onTouch(idx = touchingIdx, x = mappedX)
            return true
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            touch(cameraX.denorm(value = e2.x / widthF).roundToInt())
            val mappedX = mapX(touchingIdx, widthF)
            listener?.onTouch(idx = touchingIdx, x = mappedX)

            if (distanceX > distanceY) {
                parent.requestDisallowInterceptTouchEvent(false)
            }

            return true
        }
    })

    private fun resetTouch() {
        if (touchingFadeAnim.isRunning || touchingIdx == -1) return

        if (isBar()) {
            touchingFadeAnim.restartWith(touchingFade, 1f)
        } else {
            touch(-1)
        }

        listener?.onTouch(idx = -1, x = -1f)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (preview) return false

        gd.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = widthF
        val height = heightF

        drawer.draw(canvas)

        if (!preview) {
            yLabels.forEachByIndex {
                it.iterate(H_LINE_COUNT, it.labelPaint) { value, paint ->
                    drawLabel(canvas, value, height, paint)
                }
            }
        }
    }

    private fun drawLabel(canvas: Canvas, value: Long, height: PxF, paint: Paint): Unit =
        canvas.drawText(chartValue(value, yCamera.max), LINE_PADDING, mapY(value) - LINE_LABEL_DIST, paint)

    fun drawYLines(height: PxF, canvas: Canvas, width: PxF) {
        if (preview) return

        yLabels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.linePaint) { value, paint ->
                val y = mapY(value)
                canvas.drawLine(LINE_PADDING, y, width - LINE_PADDING, y, paint)
            }
        }
    }

    fun drawXLine(canvas: Canvas, width: PxF, height: PxF) {
        if (preview || touchingIdx == -1) return

        val mappedX = mapX(touchingIdx, width)
        canvas.drawLine(mappedX, 0f, mappedX, height, verticalLinePaint)
    }

    private fun isBar(): Boolean =
        data.type == ChartType.BAR

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (width > 0) {
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            super.onLayout(changed, l, t, r, b)
        }
    }
}
