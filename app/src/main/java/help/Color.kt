package help

import android.graphics.Color

typealias ColorString = String
typealias ColorInt = Int

val parseColor: (ColorString) -> ColorInt =
    memoize { Color.parseColor(it) }
