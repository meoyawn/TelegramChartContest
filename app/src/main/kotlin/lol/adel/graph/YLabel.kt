package lol.adel.graph

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import android.view.animation.AccelerateInterpolator
import help.*
import lol.adel.graph.widget.XLabelsView

data class YLabel(
    var min: Float,
    var max: Float,
    val linePaint: Paint,
    val labelPaint: TextPaint,
    val animator: ValueAnimator,
    var maxLabelAlpha: Norm = 1f
) {
    companion object {

        private val H_LINE_THICKNESS = 2.dpF
        private val POOL = SimplePool<YLabel>(maxPoolSize = 100)
        private val START_FAST = AccelerateInterpolator()
        private const val MAX_LINE_ALPHA = 0.1f

        /**
         * created independently
         */
        fun create(ctx: Context): YLabel =
            YLabel(
                min = 0f,
                max = 0f,
                linePaint = Paint().apply {
                    color = ctx.color(R.attr.divider)
                    strokeWidth = H_LINE_THICKNESS
                },
                animator = ValueAnimator.ofFloat(1f, 0f),
                labelPaint = TextPaint().apply {
                    color = ctx.color(R.attr.label_text)
                    textSize = XLabelsView.TEXT_SIZE_PX
                }
            )

        fun obtain(ctx: Context, list: MutableList<YLabel>, axis: YAxis): YLabel {

            val ready = POOL.acquire() ?: create(ctx).also { yLabel ->
                // created for pool
                yLabel.animator.run {
                    interpolator = START_FAST
                    addUpdateListener {
                        yLabel.setAlpha(1 - it.animatedFraction)
                    }
                    onEnd {
                        release(yLabel, list)
                    }
                }
            }

            tune(ctx, ready, axis)

            return ready
        }

        fun tune(ctx: Context, label: YLabel, axis: YAxis) {
            // theme changing
            label.linePaint.color = ctx.color(R.attr.divider)

            // reuse
            label.labelPaint.color = axis.labelColor
            label.maxLabelAlpha = axis.maxLabelAlpha
            label.setAlpha(1f)
        }

        fun release(label: YLabel, list: MutableList<YLabel>) {
            label.animator.removeAllListeners()
            label.animator.cancel()
            list -= label
            POOL.release(label)
        }
    }

    fun setAlpha(alpha: Float) {
        linePaint.alphaF = alpha * MAX_LINE_ALPHA
        labelPaint.alphaF = alpha * maxLabelAlpha
    }
}

inline fun <P:Paint> YLabel.iterate(steps: Int, paint: P, f: (Long, P) -> Unit) {
    iterate(from = min, to = max, stepSize = (max - min) / steps, f = { f(it.toLong(), paint) })
}

fun YLabel.set(from: MinMax) {
    min = from.min
    max = from.max
}

fun YLabel.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}
