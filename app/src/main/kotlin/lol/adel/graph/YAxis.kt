package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Matrix
import help.*
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round

data class YAxis(
    val camera: MinMax,
    val anticipated: MinMax,
    val labels: List<YLabel>,
    val minAnim: ValueAnimator,
    val maxAnim: ValueAnimator,
    val labelColor: ColorInt,
    val maxLabelAlpha: Norm,
    val isRight: Boolean,
    val horizontalCount: Int,
    val matrix: Matrix
) {
    companion object {
        val SIDE_PADDING = 16.dpF
        val LINE_LABEL_DIST = 5.dp
    }
}

fun YAxis.drawLabelLines(canvas: Canvas, width: PxF, split: Boolean = false) {
    val startX = when {
        !isRight ->
            YAxis.SIDE_PADDING

        split ->
            width / 2f

        else ->
            YAxis.SIDE_PADDING
    }

    val stopX = when {
        isRight ->
            width - YAxis.SIDE_PADDING

        split ->
            width / 2f

        else ->
            width - YAxis.SIDE_PADDING
    }

    labels.forEachByIndex {
        it.iterate(horizontalCount) { value ->
            val y = matrix.mapY(value)
            canvas.drawLine(startX, y, stopX, y, it.linePaint)
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
        val paint = it.labelPaint
        paint.alphaF = it.currentLabelAlpha * frac

        it.iterate(horizontalCount) { value ->
            val txt = yLabelStr(value)
            val x = when {
                isRight ->
                    width - YAxis.SIDE_PADDING - paint.measureText(txt)

                else ->
                    YAxis.SIDE_PADDING
            }
            canvas.drawText(txt, x, matrix.mapY(value) - YAxis.LINE_LABEL_DIST, paint)
        }
    }

fun YAxis.animate(new: MinMax, preview: Boolean) {
    if (new == anticipated) return

    minAnim.restartWith(camera.min, new.min)
    maxAnim.restartWith(camera.max, new.max)

    if (!preview) {
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
