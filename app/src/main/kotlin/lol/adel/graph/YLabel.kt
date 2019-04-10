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
    var maxLineAlpha: Float = 1f,
    var maxLabelAlpha: Float = 1f
) {
    companion object {

        private val H_LINE_THICKNESS = 2.dpF
        private val POOL = SimplePool<YLabel>(maxPoolSize = 100)
        private val START_FAST = AccelerateInterpolator()

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

        fun obtain(ctx: Context, list: MutableList<YLabel>, bar: Boolean): YLabel =
            POOL.acquire()?.apply { tune(ctx = ctx, label = this, bar = bar) } ?: create(ctx).also { yLabel ->
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

        fun tune(ctx: Context, label: YLabel, bar: Boolean) {
            // theme changing
            label.linePaint.color = ctx.color(R.attr.divider)
            label.labelPaint.color = if (bar) ctx.color(R.attr.label_text_bars) else ctx.color(R.attr.label_text)
            // anim reuse
            label.maxLineAlpha = 0.1f
            label.maxLabelAlpha = if (bar) 0.5f else 1f
        }

        fun release(label: YLabel, list: MutableList<YLabel>) {
            label.animator.removeAllListeners()
            label.animator.cancel()
            list -= label
            POOL.release(label)
        }
    }
}

inline fun YLabel.iterate(steps: Int, paint: Paint, f: (Long, Paint) -> Unit) {
    help.iterate(from = min, to = max, stepSize = (max - min) / steps, f = { f(it.toLong(), paint) })
}

fun YLabel.set(from: MinMax) {
    min = from.min
    max = from.max
}

fun YLabel.set(min: Float, max: Float) {
    this.min = min
    this.max = max
}

fun YLabel.setAlpha(alpha: Float) {
    linePaint.alphaF = alpha * maxLineAlpha
    labelPaint.alphaF = alpha * maxLabelAlpha
}
