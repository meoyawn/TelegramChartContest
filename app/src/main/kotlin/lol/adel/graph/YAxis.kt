package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Canvas
import help.*
import lol.adel.graph.data.ChartType
import lol.adel.graph.data.chartValue
import lol.adel.graph.widget.ChartView

data class YAxis(
    val camera: MinMax,
    val anticipated: MinMax,
    val labels: ArrayList<YLabel>,
    val minAnim: ValueAnimator,
    val maxAnim: ValueAnimator,
    val topOffset: Px,
    val bottomOffset: Px,
    val view: ChartView, // for height
    val labelColor: ColorInt,
    val maxLabelAlpha: Norm,
    val right: Boolean
) {
    private companion object {
        const val H_LINE_COUNT = 4
        val LINE_PADDING = 16.dpF
        val LINE_LABEL_DIST = 5.dp
    }

    fun effectiveHeight(): Px =
        view.height - bottomOffset - topOffset

    fun mapY(value: Long): Y =
        (1 - camera.norm(value)) * effectiveHeight() + topOffset

    fun drawLines(canvas: Canvas, width: PxF) {
        labels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.linePaint) { value, paint ->
                val y = mapY(value)
                canvas.drawLine(LINE_PADDING, y, width - LINE_PADDING, y, paint)
            }
        }
    }

    fun drawLabels(canvas: Canvas, width: PxF) {
        labels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.labelPaint) { value, paint ->
                val txt = chartValue(value, camera.max)
                val x = when {
                    right ->
                        width - LINE_PADDING - paint.measureText(txt)

                    else ->
                        LINE_PADDING
                }
                canvas.drawText(chartValue(value, camera.max), x, mapY(value) - LINE_LABEL_DIST, paint)
            }
        }
    }
}

inline fun YAxis.mapped(width: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
    f(
        view.mapX(idx = idx, width = width),
        mapY(value = points[idx])
    )

fun YAxis.animate(new: MinMax, type: ChartType) {
    if (new == anticipated) return

    minAnim.restartWith(camera.min, new.min)
    maxAnim.restartWith(camera.max, new.max)

    if (!view.preview) {
        val currentYLabel = labels.first()
        if (new.distanceSq(currentYLabel) > (currentYLabel.len() * 0.2f).sq()) {
            // appear
            currentYLabel.run {
                set(new)
                animator.restart()
            }

            // prune
            repeat(times = labels.size - 2) {
                YLabel.release(labels[1], labels)
            }

            // fade
            labels += YLabel.obtain(ctx = view.context, list = labels, axis = this).apply {
                set(anticipated)
                animator.start()
            }
        }
    }

    anticipated.set(new)
}
