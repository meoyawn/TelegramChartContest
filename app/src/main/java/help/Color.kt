package help

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat

typealias ColorString = String
typealias ColorInt = Int
typealias ColorRes = Int

val parseColor: (ColorString) -> ColorInt =
    memoize { Color.parseColor(it) }

fun Context.color(r: ColorRes): ColorInt =
    ContextCompat.getColor(ctx, r)
