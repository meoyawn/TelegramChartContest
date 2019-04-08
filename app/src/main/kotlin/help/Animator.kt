package help

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Paint
import android.view.View

inline fun animateFloat(from: Float, to: Float, crossinline f: (Float) -> Unit): ValueAnimator =
    ValueAnimator.ofFloat(from, to)
        .apply {
            addUpdateListener {
                f(denorm(it.animatedFraction, from, to))
            }
        }

inline fun animateInt(from: Int, to: Int, crossinline f: (Int) -> Unit): ValueAnimator =
    ValueAnimator.ofInt(from, to)
        .apply {
            addUpdateListener {
                f(denorm(it.animatedFraction, from, to))
            }
        }

inline fun animateColor(from: ColorInt, to: ColorInt, crossinline f: (ColorInt) -> Unit): ValueAnimator =
    ValueAnimator.ofArgb(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as ColorInt)
            }
        }

inline fun <T : Animator> T.onEnd(crossinline f: (T) -> Unit): Unit =
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            removeListener(this)
            f(this@onEnd)
        }
    })

fun playTogether(a1: Animator, a2: Animator): AnimatorSet =
    AnimatorSet().apply { playTogether(a1, a2) }

fun ValueAnimator.animatedFloat(): Float =
    animatedValue as Float

fun ValueAnimator.restartWith(from: Float, to: Float) {
    cancel()
    setFloatValues(from, to)
    start()
}

fun ValueAnimator.restart() {
    cancel()
    start()
}

// 0 to 255
typealias PaintAlpha = Int

fun View.animateAlpha(paint: Paint, to: PaintAlpha): Unit =
    animateInt(paint.alpha, to) {
        paint.alpha = it
        invalidate()
    }.start()
