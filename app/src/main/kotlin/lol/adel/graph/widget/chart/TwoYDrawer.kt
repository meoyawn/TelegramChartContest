package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.LineId
import lol.adel.graph.data.color
import lol.adel.graph.data.minMax
import lol.adel.graph.widget.ChartView

class TwoYDrawer(override val view: ChartView) : ChartDrawer {

    private val innerCirclePaint: Paint = makeInnerCirclePaint(view.context)

    private val axes: SimpleArrayMap<LineId, YAxis> =
        SimpleArrayMap<LineId, YAxis>(view.data.lineIds.size).also { map ->
            val ctx = view.context
            view.data.lineIds.forEachIndexed { idx, id ->
                val camera = MinMax()
                val axis = YAxis(
                    camera = camera,
                    anticipated = MinMax(),
                    labels = listOf(
                        YLabel.create(ctx),
                        YLabel.create(ctx)
                    ),
                    minAnim = ValueAnimator().apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        addUpdateListener {
                            camera.min = it.animatedFloat()
                            view.invalidate()
                        }
                    },
                    maxAnim = ValueAnimator().apply {
                        interpolator = AccelerateDecelerateInterpolator()
                        addUpdateListener {
                            camera.max = it.animatedFloat()
                            view.invalidate()
                        }
                    },
                    topOffset = if (view.preview) 0 else 20.dp,
                    bottomOffset = bottomOffset(),
                    view = view,
                    labelColor = view.data.color(id),
                    maxLabelAlpha = maxLabelAlpha(),
                    right = idx == 1,
                    verticalSplits = verticalSplits()
                )

                YLabel.tune(ctx, axis)

                map[id] = axis
            }
        }

    private val bitmaps = SimpleArrayMap<LineId, Bitmap>()

    override fun bottomOffset(): Px =
        if (view.preview) 0 else 5.dp

    override fun initYAxis() {
        val data = view.data
        axes.forEach { id, axis ->
            axis.anticipated.set(data.minMax(view.cameraX, id))
            axis.camera.set(axis.anticipated)
            axis.labels.first().set(axis.camera)
        }
    }

    override fun animateYAxis() {
        val data = view.data
        view.enabledLines.forEachByIndex { id ->
            axes[id]!!.animate(data.minMax(view.cameraX, id))
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    private fun drawLine(
        column: AnimatedColumn,
        id: LineId,
        width: PxF,
        start: Float,
        buf: FloatArray,
        end: Float,
        canvas: Canvas
    ) {
        val points = column.points
        val axis = axes[id]!!

        axis.mapped(width, points, start.floor()) { x, y ->
            // start of first line
            buf[0] = x
            buf[1] = y
        }

        var bufIdx = 2
        for (i in start.ceil()..end.ceil()) {
            axis.mapped(width, points, i) { x, y ->
                buf[bufIdx + 0] = x
                buf[bufIdx + 1] = y
                buf[bufIdx + 2] = x
                buf[bufIdx + 3] = y

                bufIdx += 4
            }
        }
        bufIdx -= 2

        column.paint.alphaF = if (view.preview) 1f else column.frac
        canvas.drawLines(buf, 0, bufIdx, column.paint)
    }

    private fun drawPreview(canvas: Canvas) {
        val columns = view.animatedColumns

        val (start, end) = view.cameraX
        val height = view.height
        val width = view.width

        columns.forEach { id, column ->
            if (column.frac > 0) {
                val bmp = bitmaps[id]
                    ?: Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444).also {
                        drawLine(column, id, width.toFloat(), start, view.lineBuf, end, Canvas(it))
                        bitmaps[id] = it
                    }

                column.paint.alphaF = column.frac
                canvas.drawBitmap(bmp, 0f, 0f, column.paint)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (view.preview) {
            drawPreview(canvas)
            return
        }

        val (start, end) = view.cameraX
        val height = view.heightF
        val width = view.widthF

        view.drawXLine(canvas, width, height)

        val buf = view.lineBuf

        val columns = view.animatedColumns

        val leftId = columns.keyAt(0)
        val leftColumn = columns.valueAt(0)
        val rightId = columns.keyAt(1)
        val rightColumn = columns.valueAt(1)

        val split = leftColumn.frac > 0 && rightColumn.frac > 0
        columns.forEach { id, column ->
            if (column.frac > 0) {
                axes[id]!!.drawLines(canvas, width, split = split)
            }
        }

        columns.forEach { id, column ->
            if (column.frac > 0) {
                drawLine(column, id, width, start, buf, end, canvas)
            }
        }

        if (view.touchingIdx != -1) {
            columns.forEach { id, column ->
                if (column.frac > 0) {
                    val axis = axes[id]!!
                    axis.mapped(width, column.points, view.touchingIdx) { x, y ->
                        canvas.drawCircle(x, y, LineDrawer.OUTER_CIRCLE_RADIUS, column.paint)
                        canvas.drawCircle(x, y, LineDrawer.INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }
        }

        if (leftColumn.frac > 0) {
            axes[leftId]!!.drawLabels(canvas, width, frac = leftColumn.frac)
        }
        if (rightColumn.frac > 0) {
            axes[rightId]!!.drawLabels(canvas, width, frac = rightColumn.frac)
        }
    }
}
