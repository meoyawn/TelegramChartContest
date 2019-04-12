package lol.adel.graph

import android.util.LongSparseArray
import java.text.SimpleDateFormat
import java.util.*

object Dates {

    private fun func(instant: Long, cache: LongSparseArray<String>, format: SimpleDateFormat): String =
        cache[instant] ?: format.format(instant).also { cache.put(instant, it) }

    private val PANEL = SimpleDateFormat("EEE, d MMM yyyy", Locale.US)
    private val tooltipCache = LongSparseArray<String>()
    fun tooltip(instant: Long): String =
        func(instant, tooltipCache, PANEL)

    private val horizontalFmt = SimpleDateFormat("MMM d", Locale.US)
    private val horizontalCache = LongSparseArray<String>()
    fun xLabel(instant: Long): String =
        func(instant, horizontalCache, horizontalFmt)

    private val headerFmt = SimpleDateFormat("d MMMM yyyy", Locale.US)
    private val headerCache = LongSparseArray<String>()
    fun header(instant: Long): String =
        func(instant, headerCache, headerFmt)
}
