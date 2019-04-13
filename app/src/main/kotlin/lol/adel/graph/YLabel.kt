package lol.adel.graph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import help.*
import lol.adel.graph.widget.XLabelsView

data class YLabel(
    val value: MinMax,
    val linePaint: Paint,
    val labelPaint: TextPaint,
    val animator: ValueAnimator,
    var maxLabelAlpha: Norm = 1f,
    var currentLabelAlpha: Norm = 1f
) {
    companion object {

        private val H_LINE_THICKNESS = 1.dpF
        private const val MAX_LINE_ALPHA = 0.1f

        /**
         * created independently
         */
        fun create(ctx: Context): YLabel =
            YLabel(
                value = MinMax(),
                linePaint = Paint().apply {
                    color = ctx.color(R.attr.divider)
                    strokeWidth = H_LINE_THICKNESS
                },
                animator = valueAnimator(),
                labelPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = ctx.color(R.attr.label_text)
                    textSize = XLabelsView.TEXT_SIZE_PX
                }
            )

        fun tune(ctx: Context, axis: YAxis) {
            axis.labels.first().run {
//                animator.interpolator = AccelerateInterpolator()
                animator.addUpdateListener {
                    setAlpha(it.animatedFraction)
                }
                tune(ctx, this, axis)
                setAlpha(1f)
            }
            axis.labels.last().run {
                animator.run {
//                    interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        setAlpha(1 - it.animatedFraction)
                    }
                }
                tune(ctx, this, axis)
                setAlpha(0f)
            }
        }

        private fun tune(ctx: Context, label: YLabel, axis: YAxis) {
            // theme changing
            label.linePaint.color = ctx.color(R.attr.divider)

            // reuse
            label.labelPaint.color = axis.labelColor
            label.maxLabelAlpha = axis.maxLabelAlpha
        }
    }

    fun setAlpha(alpha: Float) {
        linePaint.alphaF = alpha * MAX_LINE_ALPHA

        currentLabelAlpha = alpha * maxLabelAlpha
        labelPaint.alphaF = currentLabelAlpha
    }
}

inline fun YLabel.iterate(steps: Int, f: (Long) -> Unit) {
    if (value.empty()) return
    iterate(from = value.min, to = value.max, stepSize = (value.max - value.min) / steps, f = { f(it.toLong()) })
}

fun YLabel.set(from: MinMax) {
    value.set(from)
}

fun YLabel.set(min: Float, max: Float) {
    value.set(min, max)
}
