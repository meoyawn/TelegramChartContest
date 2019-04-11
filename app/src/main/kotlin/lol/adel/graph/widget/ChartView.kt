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

    val topOffset = if (preview) 0 else 20.dp

    val yAxis = run {
        val camera = MinMax()
        YAxis(
            camera = camera,
            anticipated = MinMax(),
            labels = ArrayList(),
            minAnim = ValueAnimator().apply {
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    camera.min = it.animatedFloat()
                    invalidate()
                }
            },
            maxAnim = ValueAnimator().apply {
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    camera.max = it.animatedFloat()
                    invalidate()
                }
            },
            topOffset = topOffset,
            bottomOffset = drawer.bottomOffset(),
            view = this,
            labelColor = drawer.labelColor(),
            maxLabelAlpha = drawer.maxLabelAlpha(),
            right = false
        )
    }

    fun mapX(idx: Idx, width: PxF): X =
        cameraX.norm(idx) * width

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

        drawer.draw(canvas)

        if (!preview) {
            yAxis.drawLabels(canvas, widthF)
        }
    }

    fun drawYLines(canvas: Canvas, width: PxF) {
        if (preview) return

        yAxis.drawLines(canvas, width)
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
