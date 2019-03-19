package help

import android.content.Context
import android.graphics.Color
import android.os.Build

typealias ColorString = String
typealias ColorInt = Int
typealias ColorRes = Int

val parseColor: (ColorString) -> ColorInt =
    memoize { Color.parseColor(it) }

fun Context.color(r: ColorRes): ColorInt =
    if (Build.VERSION.SDK_INT >= 23) getColor(r)
    else ctx.resources.getColor(r)
