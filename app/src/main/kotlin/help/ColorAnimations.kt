package help

import android.animation.ValueAnimator
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView

// 0 to 255
typealias PaintAlpha = Int

fun View.animateAlpha(paint: Paint, to: PaintAlpha): Unit =
    animateInt(paint.alpha, to) {
        paint.alpha = it
        invalidate()
    }.start()

fun View.animateAlpha(paint1: Paint, paint2: Paint, to: PaintAlpha): ValueAnimator =
    animateInt(paint1.alpha, to) {
        paint1.alpha = it
        paint2.alpha = it
        invalidate()
    }.apply { start() }

fun View.animateColor(paint: Paint, toRes: ColorRes) {
    val alpha = paint.alpha

    animateColor(paint.color, color(toRes)) {
        paint.color = it
        paint.alpha = alpha

        invalidate()
    }.start()
}

fun View.animateColor(paint1: Paint, paint2: Paint, toRes: ColorRes) {
    val alpha1 = paint1.alpha
    val alpha2 = paint2.alpha

    animateColor(paint1.color, color(toRes)) {
        paint1.color = it
        paint1.alpha = alpha1

        paint2.color = it
        paint2.alpha = alpha2

        invalidate()
    }.start()
}

fun ColorDrawable.animate(toInt: ColorInt): Unit =
    animateColor(color, toInt) {
        color = it
    }.start()

fun TextView.animateTextColor(to: ColorRes): Unit =
    animateColor(currentTextColor, color(to)) {
        setTextColor(it)
    }.start()

fun View.animateBackground(to: ColorRes): Unit =
    animateColor(background.let { it as ColorDrawable }.color, color(to)) {
        setBackgroundColor(it)
    }.start()