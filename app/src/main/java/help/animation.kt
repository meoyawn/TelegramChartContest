package help

import android.animation.ValueAnimator

inline fun <T> T.animateFloat(from: Float, to: Float, crossinline f: T.(Float) -> Unit): ValueAnimator =
    ValueAnimator.ofFloat(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as Float)
            }
        }
