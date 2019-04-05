package help

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup

typealias Dp = Int
typealias DpF = Float

typealias Px = Int

val Context.ctx: Context
    get() = this

val Activity.act: Activity
    get() = this

private fun dpToPx(dp: DpF): PxF =
    dp * Resources.getSystem().displayMetrics.density

val DpF.dp: PxF
    get() = dpToPx(dp = this)

val Dp.dpF: PxF
    get() = dpToPx(dp = toFloat())

val Dp.dp: Px
    get() = dpF.toInt()

val Px.px: Px
    get() = this

val View.widthF: PxF
    get() = width.toFloat()

val View.heightF: PxF
    get() = height.toFloat()

const val MATCH_PARENT = ViewGroup.MarginLayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.MarginLayoutParams.WRAP_CONTENT

private val TV = TypedValue()

fun Context.attr(id: Int): Int {
    theme.resolveAttribute(id, TV, true)
    return TV.resourceId
}
