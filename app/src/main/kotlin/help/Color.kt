package help

import android.content.Context
import android.os.Build
import android.view.View

typealias ColorInt = Int
typealias ColorRes = Int

fun Context.color(r: ColorRes): ColorInt =
    if (Build.VERSION.SDK_INT >= 23) getColor(r)
    else ctx.resources.getColor(r)

fun View.color(r: ColorRes): ColorInt =
    context.color(r)
