package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.view.animation.DecelerateInterpolator
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.LineId
import lol.adel.graph.data.color
import lol.adel.graph.data.minMax
import lol.adel.graph.widget.ChartView
import kotlin.math.round
import kotlin.math.roundToInt

class TwoYDrawer(override val view: ChartView) : ChartDrawer {

    private val innerCirclePaint = makeInnerCirclePaint(view.context)

    private var touchingX: X = -1f
    private var touchingIdx: IdxF = -1f
    private val touchUp = ValueAnimator().apply {
        addUpdateListener {
            val idx = it.animatedFloat()
            touch(idx, matrix.mapX(idx))
        }
    }

    private val bottomOffset = if (view.preview) 0 else 5.dp
    private val matrix = Matrix()

    private val axes: SimpleArrayMap<LineId, YAxis> =
        SimpleArrayMap<LineId, YAxis>(view.data.lineIds.size).also { map ->
            val ctx = view.context
            view.data.lineIds.forEachIndexed { idx, id ->
                val camera = MinMax()
                map[id] = YAxis(
                    camera = camera,
                    anticipated = MinMax(),
                    labels = listOf(
                        YLabel.create(ctx),
                        YLabel.create(ctx)
                    ),
                    minAnim = ValueAnimator().apply {
                        interpolator = DecelerateInterpolator(2f)
                        addUpdateListener {
                            camera.min = it.animatedFloat()
                            view.invalidate()
                        }
                    },
                    maxAnim = ValueAnimator().apply {
                        interpolator = DecelerateInterpolator(2f)
                        addUpdateListener {
                            camera.max = it.animatedFloat()
                            view.invalidate()
                        }
                    },
                    labelColor = view.data.color(id),
                    maxLabelAlpha = maxLabelAlpha(),
                    isRight = idx == 1,
                    horizontalCount = verticalSplits(),
                    matrix = Matrix()
                ).also {
                    YLabel.tune(ctx, it)
                }
            }
        }

    private val curveBitmaps = SimpleArrayMap<LineId, Bitmap>()

    override fun touch(idx: IdxF, x: X) {
        touchingX = x
        touchingIdx = idx
        view.listener?.onTouch(touchingIdx.roundToInt(), touchingX)
        view.invalidate()
    }

    override fun touchUp() {
        if (touchingIdx < 0) return

        touchUp.restartWith(touchingIdx, round(touchingIdx))
    }

    override fun touchClear() {
        if (touchingIdx < 0) return

        touchingX = -1f
        touchingIdx = -1f
        view.listener?.onTouch(touchingIdx.roundToInt(), touchingX)
        view.invalidate()
    }

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
            axes[id]!!.animate(new = data.minMax(view.cameraX, id), preview = view.preview)
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    private fun drawCurve(column: AnimatedColumn, buf: FloatArray, canvas: Canvas, matrix: Matrix) {
        val bufIdx = fillCurve(column.points, buf, view.cameraX)
        matrix.mapPoints(buf, 0, buf, 0, bufIdx)
        column.paint.alphaF = column.frac
        canvas.drawLines(buf, 0, bufIdx, column.paint)
    }

    private fun drawPreview(canvas: Canvas, width: PxF, height: PxF) {
        view.animatedColumns.forEach { id, column ->
            if (column.frac > 0) {
                val bmp = curveBitmaps[id]
                    ?: Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_4444).also {
                        drawCurve(column, view.lineBuf, Canvas(it), axes[id]!!.matrix)
                        curveBitmaps[id] = it
                    }

                column.paint.alphaF = column.frac
                canvas.drawBitmap(bmp, 0f, 0f, column.paint)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        val width = view.widthF
        val height = view.heightF

        axes.forEachValue {
            it.matrix.setup(
                cameraX = view.cameraX,
                cameraY = it.camera,
                right = width,
                bottom = height - bottomOffset,
                top = view.topOffset
            )
        }

        if (view.preview) {
            drawPreview(canvas, width, height)
            return
        }

        val columns = view.animatedColumns

        val leftId = columns.keyAt(0)
        val leftColumn = columns.valueAt(0)
        val rightId = columns.keyAt(1)
        val rightColumn = columns.valueAt(1)

        val split = leftColumn.frac > 0 && rightColumn.frac > 0
        columns.forEach { id, column ->
            if (column.frac > 0) {
                axes[id]!!.drawLabelLines(canvas, width, split)
            }
        }

        columns.forEach { id, column ->
            if (column.frac > 0) {
                drawCurve(column, view.lineBuf, canvas, axes[id]!!.matrix)
            }
        }

        if (touchingIdx >= 0f) {
            val x = touchingX
            columns.forEach { _, column ->
                if (column.frac > 0) {
                    val y = matrix.mapY(interpolate(touchingIdx, column.points))
                    canvas.drawCircle(x, y, LineDrawer.OUTER_CIRCLE_RADIUS, column.paint)
                    canvas.drawCircle(x, y, LineDrawer.INNER_CIRCLE_RADIUS, innerCirclePaint)
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
