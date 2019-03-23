package help

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

inline fun animateFloat(from: Float, to: Float, crossinline f: (Float) -> Unit): ValueAnimator =
    ValueAnimator.ofFloat(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as Float)
            }
        }

inline fun animateInt(from: Int, to: Int, crossinline f: (Int) -> Unit): ValueAnimator =
    ValueAnimator.ofInt(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as Int)
            }
        }

inline fun animateColor(from: ColorInt, to: ColorInt, crossinline f: (ColorInt) -> Unit): ValueAnimator =
    ValueAnimator.ofArgb(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as ColorInt)
            }
        }

inline fun Animator.onEnd(crossinline f: () -> Unit) =
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            f()
        }
    })
