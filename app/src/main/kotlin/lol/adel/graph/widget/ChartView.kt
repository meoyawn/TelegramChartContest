package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*
import lol.adel.graph.widget.chart.*
import kotlin.math.abs
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
            topOffset = if (preview) 0 else 20.dp,
            bottomOffset = drawer.bottomOffset(),
            view = this,
            labelColor = drawer.labelColor(),
            maxLabelAlpha = drawer.maxLabelAlpha(),
            right = false,
            verticalSplits = drawer.verticalSplits()
        )
    }

    fun mapX(idx: Idx, width: PxF): X =
        cameraX.norm(idx) * width

    //region Touch Feedback
    var touchingIdx: Idx = -1
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
        if (touchingIdx != -1) {
            touchingIdx = -1
            listener?.onTouch(idx = touchingIdx, x = -1f)
            invalidate()
        }

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

    private var touchX: X = -1f
    private var touchY: Y = -1f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (preview) return false

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val evX = event.x
                val evY = event.y

                if (touchX != -1f) {
                    if (abs(evX - touchX) < abs(evY - touchY)) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }

                touchX = evX
                touchY = evY

                val width = widthF
                touchingIdx = cameraX.denorm(value = evX / width).roundToInt()
                val mappedX = mapX(touchingIdx, width)
                listener?.onTouch(idx = touchingIdx, x = mappedX)
                invalidate()
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchY = -1f
                touchX = -1f
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

        yAxis.drawLines(canvas, width, split = false)
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
