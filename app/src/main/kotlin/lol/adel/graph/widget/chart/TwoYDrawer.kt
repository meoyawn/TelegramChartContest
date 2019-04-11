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
import lol.adel.graph.data.minMax
import lol.adel.graph.widget.ChartView

class TwoYDrawer(override val view: ChartView) : ChartDrawer {

    private val innerCirclePaint: Paint = makeInnerCirclePaint(view.context)

    private val axes: SimpleArrayMap<LineId, YAxis> = view.data.lineIds.toSimpleArrayMap {
        val camera = MinMax()
        YAxis(
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
            topOffset = view.topOffset,
            bottomOffset = bottomOffset(),
            view = view
        )
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
                YLabel.tune(ctx = ctx, label = this, isBar = false)
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
        view.enabledLines.forEachByIndex { id ->
            val axis = axes[id]!!
            val temp = data.minMax(view.cameraX, id)

            if (temp != axis.anticipated) {
                axis.minAnim.restartWith(axis.camera.min, temp.min)
                axis.maxAnim.restartWith(axis.camera.max, temp.max)

                if (!view.preview) {
                    axis.animate(temp, data.type)
                }

                axis.anticipated.set(temp)
            }
        }
    }

    override fun makePaint(clr: ColorInt): Paint =
        makeLinePaint(view.preview, clr)

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX
        val height = view.heightF
        val width = view.widthF

        view.drawYLines(canvas, width)
        view.drawXLine(canvas, width, height)

        val buf = view.lineBuf

        if (!view.preview) {
            view.animatedColumns.forEach { id, column ->
                val axis = axes[id]!!
                axis.drawLines(canvas, width)
            }
        }

        view.animatedColumns.forEach { id, column ->
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

        if (!view.preview && view.touchingIdx != -1) {
            view.animatedColumns.forEach { id, column ->
                if (column.frac > 0) {
                    val axis = axes[id]!!
                    axis.mapped(width, column.points, view.touchingIdx) { x, y ->
                        canvas.drawCircle(x, y, LineDrawer.OUTER_CIRCLE_RADIUS, column.paint)
                        canvas.drawCircle(x, y, LineDrawer.INNER_CIRCLE_RADIUS, innerCirclePaint)
                    }
                }
            }
        }

        if (!view.preview) {
            view.animatedColumns.forEach { id, column ->
                val axis = axes[id]!!
                axis.drawLabels(canvas)
            }
        }
    }
}
