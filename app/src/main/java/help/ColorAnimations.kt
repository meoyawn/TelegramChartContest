package help

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

fun View.animateColor(paint: Paint, toRes: ColorRes): Unit =
    animateColor(paint.color, color(toRes)) {
        paint.color = it
        invalidate()
    }.start()

fun View.animateColor(paint1: Paint, paint2: Paint, toRes: ColorRes): Unit =
    animateColor(paint1.color, color(toRes)) {
        paint1.color = it
        paint2.color = it
        invalidate()
    }.start()

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
