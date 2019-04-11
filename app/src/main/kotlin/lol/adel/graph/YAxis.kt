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
    val color: ColorInt
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

    fun drawLabels(canvas: Canvas) {
        labels.forEachByIndex {
            it.iterate(H_LINE_COUNT, it.labelPaint) { value, paint ->
                canvas.drawText(chartValue(value, camera.max), LINE_PADDING, mapY(value) - LINE_LABEL_DIST, paint)
            }
        }
    }
}

inline fun YAxis.mapped(width: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
    f(
        view.mapX(idx = idx, width = width),
        mapY(value = points[idx])
    )

fun YAxis.animate(tempY: MinMax, type: ChartType) {
    val currentYLabel = labels.first()
    if (tempY.distanceSq(currentYLabel) <= (currentYLabel.len() * 0.2f).sq()) return

    // appear
    currentYLabel.run {
        set(tempY)
        animator.restart()
    }

    // prune
    repeat(times = labels.size - 3) {
        YLabel.release(labels[1], labels)
    }

    // fade
    labels += YLabel.obtain(ctx = view.context, list = labels, isBar = type == ChartType.BAR).apply {
        set(anticipated)
        animator.start()
    }
}
