package help

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator

inline fun <T> T.animateFloat(from: Float, to: Float, crossinline f: T.(Float) -> Unit): ValueAnimator =
    ValueAnimator.ofFloat(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as Float)
            }
        }

inline fun <T> T.animateInt(from: Int, to: Int, crossinline f: T.(Int) -> Unit): ValueAnimator =
    ValueAnimator.ofInt(from, to)
        .apply {
            addUpdateListener {
                f(it.animatedValue as Int)
            }
        }

inline fun <T> T.animateLong(from: Long, to: Long, crossinline f: T.(Long) -> Unit): ValueAnimator =
    ValueAnimator.ofInt(from.toInt(), to.toInt())
        .apply {
            addUpdateListener {
                f((it.animatedValue as Int).toLong())
            }
        }

fun playSequentially(vararg a: Animator): AnimatorSet =
    AnimatorSet().apply { playSequentially(*a) }

fun playTogether(vararg a: Animator): AnimatorSet =
    AnimatorSet().apply { playTogether(*a) }

fun <T : ValueAnimator> T.chainUpdateListener(ul: ValueAnimator.AnimatorUpdateListener): T =
    apply { addUpdateListener(ul) }

inline fun <reified T> animationUpdate(crossinline f: (T) -> Unit): ValueAnimator.AnimatorUpdateListener =
    ValueAnimator.AnimatorUpdateListener {
        f(it.animatedValue as T)
    }

inline fun <reified T> ValueAnimator.onUpdate(crossinline f: (T) -> Unit): ValueAnimator =
    chainUpdateListener(animationUpdate<T>(f))
