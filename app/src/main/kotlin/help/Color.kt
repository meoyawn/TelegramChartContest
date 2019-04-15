package help

import android.content.Context
import android.util.TypedValue
import android.view.View

typealias AttrRes = Int
typealias ColorInt = Int

private val TV = TypedValue()

fun Context.color(id: AttrRes): ColorInt {
    theme.resolveAttribute(id, TV, true)
    return TV.data
}

fun Context.attr(id: AttrRes): Int {
    theme.resolveAttribute(id, TV, true)
    return TV.resourceId
}

fun View.color(res: AttrRes): ColorInt =
    context.color(res)

fun argb(fraction: Float, startValue: ColorInt, endValue: ColorInt): ColorInt {
    val startA = (startValue shr 24 and 0xff) / 255.0f
    var startR = (startValue shr 16 and 0xff) / 255.0f
    var startG = (startValue shr 8 and 0xff) / 255.0f
    var startB = (startValue and 0xff) / 255.0f

    val endA = (endValue shr 24 and 0xff) / 255.0f
    var endR = (endValue shr 16 and 0xff) / 255.0f
    var endG = (endValue shr 8 and 0xff) / 255.0f
    var endB = (endValue and 0xff) / 255.0f

    // convert from sRGB to linear
    startR = Math.pow(startR.toDouble(), 2.2).toFloat()
    startG = Math.pow(startG.toDouble(), 2.2).toFloat()
    startB = Math.pow(startB.toDouble(), 2.2).toFloat()

    endR = Math.pow(endR.toDouble(), 2.2).toFloat()
    endG = Math.pow(endG.toDouble(), 2.2).toFloat()
    endB = Math.pow(endB.toDouble(), 2.2).toFloat()

    // compute the interpolated color in linear space
    var a = startA + fraction * (endA - startA)
    var r = startR + fraction * (endR - startR)
    var g = startG + fraction * (endG - startG)
    var b = startB + fraction * (endB - startB)

    // convert back to sRGB in the [0..255] range
    a *= 255.0f
    r = Math.pow(r.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
    g = Math.pow(g.toDouble(), 1.0 / 2.2).toFloat() * 255.0f
    b = Math.pow(b.toDouble(), 1.0 / 2.2).toFloat() * 255.0f

    return Math.round(a) shl 24 or (Math.round(r) shl 16) or (Math.round(g) shl 8) or Math.round(b)
}
