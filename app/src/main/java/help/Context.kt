package help

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import kotlin.math.roundToInt

typealias Dp = Int
typealias DpF = Float

typealias Px = Int
typealias PxF = Float

val Context.ctx: Context
    get() = this

private fun dpToPx(dp: DpF): PxF =
    dp * Resources.getSystem().displayMetrics.density

val Dp.dp: Px
    get() = dpToPx(toFloat()).roundToInt()

val Dp.dpF: PxF
    get() = dpToPx(toFloat())

val View.widthF: PxF
    get() = width.toFloat()

val View.heightF: PxF
    get() = height.toFloat()

const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
