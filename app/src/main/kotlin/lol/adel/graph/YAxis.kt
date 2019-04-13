package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Canvas
import help.*
import lol.adel.graph.widget.ChartView
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

data class YAxis(
    val camera: MinMax,
    val anticipated: MinMax,
    val labels: List<YLabel>,
    val minAnim: ValueAnimator,
    val maxAnim: ValueAnimator,
    val topOffset: Px,
    val bottomOffset: Px,
    val view: ChartView, // for height
    val labelColor: ColorInt,
    val maxLabelAlpha: Norm,
    val right: Boolean,
    val verticalSplits: Int
) {

    companion object {
        val LINE_PADDING = 16.dpF
        val LINE_LABEL_DIST = 5.dp
    }

    fun effectiveHeight(): Px =
        view.height - bottomOffset - topOffset

    fun mapY(value: Long): Y =
        (1 - camera.norm(value)) * effectiveHeight() + topOffset

    fun drawLines(canvas: Canvas, width: PxF, split: Boolean) {

        val startX = when {
            !right ->
                LINE_PADDING

            split ->
                width / 2f

            else ->
                LINE_PADDING
        }

        val stopX = when {
            right ->
                width - LINE_PADDING

            split ->
                width / 2f

            else ->
                width - LINE_PADDING
        }

        labels.forEachByIndex {
            if (it.currentLabelAlpha > 0) {
                it.iterate(verticalSplits) { value ->
                    val y = mapY(value)
                    canvas.drawLine(startX, y, stopX, y, it.linePaint)
                }
            }
        }
    }
}

private fun rnd(value: Double): String {
    val prettyRound = round(value * 10) / 10
    val floor = floor(prettyRound).toLong()
    val point = prettyRound - floor

    return if (point == 0.0) {
        "$floor"
    } else {
        "$floor.${(point * 10).toInt()}"
    }
}

private fun yLabelStr(value: Long): String =
    when (abs(value)) {
        in 0..1_000 ->
            "$value"

        in 1000..1_000_000 ->
            "${rnd(value = value / 1_000.0)}K"

        else ->
            "${rnd(value = value / 1_000_000.0)}M"
    }

fun YAxis.drawLabels(canvas: Canvas, width: PxF, frac: Norm = 1f): Unit =
    labels.forEachByIndex {
        if (it.currentLabelAlpha > 0) {
            val paint = it.labelPaint
            paint.alphaF = it.currentLabelAlpha * frac

            it.iterate(verticalSplits) { value ->
                val txt = yLabelStr(value)
                val x = when {
                    right ->
                        width - YAxis.LINE_PADDING - paint.measureText(txt)
                    else ->
                        YAxis.LINE_PADDING
                }
                canvas.drawText(txt, x, mapY(value) - YAxis.LINE_LABEL_DIST, paint)
            }
        }
    }


inline fun YAxis.mapped(width: PxF, points: LongArray, idx: Idx, f: (x: X, y: Y) -> Unit): Unit =
    f(
        view.mapX(idx = idx, width = width),
        mapY(value = points[idx])
    )

fun YAxis.animate(new: MinMax) {
    if (new == anticipated) return

    minAnim.restartWith(camera.min, new.min)
    maxAnim.restartWith(camera.max, new.max)

    if (!view.preview) {
        val single = labels.last().currentLabelAlpha == 0f

        labels.first().run {
            set(new)
            if (single) {
                animator.start()
            }
        }

        if (single) {
            labels.last().run {
                set(anticipated)
                animator.start()
            }
        }
    }

    anticipated.set(new)
}
