package lol.adel.graph.widget.chart

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
            view.data.lineIds.forEachIndexed { idx, id ->
                val camera = MinMax()
                map[id] = YAxis(
                    camera = camera,
                    anticipated = MinMax(),
                    labels = ArrayList(),
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
            }
        }

    override fun bottomOffset(): Px =
        if (view.preview) 0 else 5.dp

    override fun initYAxis() {
        val ctx = view.context
        val data = view.data
        data.lineIds.forEachByIndex { id ->
            val axis = axes[id]!!

            axis.anticipated.set(data.minMax(view.cameraX, id))
            axis.camera.set(axis.anticipated)

            axis.labels += YLabel.create(ctx).apply {
                YLabel.tune(ctx = ctx, label = this, axis = axis)
                animator.interpolator = DecelerateInterpolator()
                animator.addUpdateListener {
                    setAlpha(it.animatedFraction)
                }
                set(axis.camera)
            }
        }
    }

    override fun animateYAxis() {
        val data = view.data

        val columns = view.animatedColumns
        val leftColumn = columns.valueAt(0)
        val rightColumn = columns.valueAt(1)
        val split = leftColumn.frac > 0 && rightColumn.frac > 0

        view.enabledLines.forEachByIndex { id ->
            axes[id]!!.animate(data.minMax(view.cameraX, id))
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun draw(canvas: Canvas) {
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

        if (!view.preview) {
            val split = leftColumn.frac > 0 && rightColumn.frac > 0
            columns.forEach { id, column ->
                if (column.frac > 0) {
                    axes[id]!!.drawLines(canvas, width, split = split)
                }
            }
        }

        columns.forEach { id, column ->
            if (column.frac > 0) {
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

                column.paint.alphaF = column.frac
                canvas.drawLines(buf, 0, bufIdx, column.paint)
            }
        }

        if (!view.preview) {
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
}
