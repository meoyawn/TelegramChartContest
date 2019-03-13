package help

import android.view.View

fun View.updatePadding(
    left: Px = paddingLeft,
    top: Px = paddingTop,
    right: Px = paddingRight,
    bottom: Px = paddingBottom
): Unit =
    setPadding(left, top, right, bottom)
