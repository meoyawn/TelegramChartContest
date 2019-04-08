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
    val animator: ValueAnimator
) {
    companion object {

        private val H_LINE_THICKNESS = 2.dpF
        private val POOL = SimplePool<YLabel>(maxPoolSize = 100)
        private val START_FAST = AccelerateInterpolator()

        /**
         * created independently
         */
        fun create(ctx: Context): YLabel {
            val linePaint = Paint().apply {
                color = ctx.color(R.color.divider)
                strokeWidth = H_LINE_THICKNESS
            }

            val labelPaint = TextPaint().apply {
                color = ctx.color(R.color.label_text)
                textSize = XLabelsView.TEXT_SIZE_PX
            }

            return YLabel(
                min = 0f,
                max = 0f,
                linePaint = linePaint,
                animator = ValueAnimator.ofFloat(1f, 0f),
                labelPaint = labelPaint
            )
        }

        fun obtain(ctx: Context, list: MutableList<YLabel>): YLabel =
            POOL.acquire()?.apply {
                // theme changing
                linePaint.color = ctx.color(R.color.divider)
                labelPaint.color = ctx.color(R.color.label_text)
            } ?: create(ctx).also { yLabel ->
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

        fun release(label: YLabel, list: MutableList<YLabel>) {
            label.animator.removeAllListeners()
            label.animator.cancel()
            list -= label
            POOL.release(label)
        }
    }
}

inline fun YLabel.iterate(steps: Int, paint: Paint, f: (Long, Paint) -> Unit) {
    val origStepSize = (max - min) / steps
    val newMax = max - origStepSize / 3
    val newStepSize = (newMax - min) / steps
    help.iterate(from = min, to = newMax, stepSize = newStepSize, f = { f(it.toLong(), paint) })
}

fun YLabel.set(from: MinMax) {
    min = from.min
    max = from.max
}

fun YLabel.setAlpha(alpha: Float) {
    labelPaint.alphaF = alpha
    linePaint.alphaF = alpha
}
