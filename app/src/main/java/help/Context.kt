package help

import android.content.Context
import android.content.res.Resources
import kotlin.math.roundToInt

val Context.ctx: Context
    get() = this

private fun dpToPx(dp: Float): Float =
    dp * Resources.getSystem().displayMetrics.density

val Int.dp: Int
    get() = dpToPx(toFloat()).roundToInt()
