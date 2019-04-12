package lol.adel.graph

import android.util.LongSparseArray
import java.text.SimpleDateFormat
import java.util.*

object Dates {
    val PANEL = SimpleDateFormat("EEE, MMM d", Locale.US)

    private val HORIZONTAL = SimpleDateFormat("MMM d", Locale.US)
    private val horizontals = LongSparseArray<String>()

    fun formatX(instant: Long): String =
        horizontals[instant] ?: HORIZONTAL.format(instant).also { horizontals.put(instant, it) }

    val HEADER_RANGE = SimpleDateFormat(" d MMMM yyyy", Locale.US)
}
