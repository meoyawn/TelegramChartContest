package help

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import lol.adel.graph.widget.TextDiffView

fun View.updatePadding(
    left: Px = paddingLeft,
    top: Px = paddingTop,
    right: Px = paddingRight,
    bottom: Px = paddingBottom
): Unit =
    setPadding(left, top, right, bottom)

typealias Visibility = Int

fun visibleOrGone(b: Boolean): Visibility =
    if (b) View.VISIBLE else View.GONE

fun visibleOrInvisible(b: Boolean): Visibility =
    if (b) View.VISIBLE else View.INVISIBLE

inline fun ViewGroup.forEach(f: (View) -> Unit): Unit =
    repeat(childCount) {
        f(getChildAt(it))
    }

operator fun ViewGroup.plusAssign(v: View): Unit =
    addView(v)
