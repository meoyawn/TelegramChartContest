package help

import android.content.Context
import android.util.TypedValue
import android.view.View

typealias AttrRes = Int
typealias ColorInt = Int

inline class ColorRes(val res: Int)

private val TV = TypedValue()

fun Context.color(id: AttrRes): ColorInt {
    theme.resolveAttribute(id, TV, true)
    return TV.data
}

fun View.color(res: AttrRes): ColorInt =
    context.color(res)
