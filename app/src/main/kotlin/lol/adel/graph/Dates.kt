package lol.adel.graph

import android.util.LongSparseArray
import java.text.SimpleDateFormat
import java.util.*


object Dates {

    private fun func(instant: Long, cache: LongSparseArray<String>, format: SimpleDateFormat): String {
        println("cache size ${cache.size()}")
        return cache[instant] ?: format.format(instant).also { cache.put(instant, it) }
    }

    val PANEL = SimpleDateFormat("EEE, d MMM yyyy", Locale.US)

    private val horizontalFmt = SimpleDateFormat("MMM d", Locale.US)
    private val horizontalCache = LongSparseArray<String>()
    fun formatX(instant: Long): String =
        func(instant, horizontalCache, horizontalFmt)

    private val headerFmt = SimpleDateFormat(" d MMMM yyyy", Locale.US)
    private val headerCache = LongSparseArray<String>()

    fun header(instant: Long): String =
        func(instant, headerCache, headerFmt)
}
