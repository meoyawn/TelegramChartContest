package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import help.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.EMPTY_CHART
import lol.adel.graph.data.x
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import kotlin.math.roundToInt


class HorizontalLabelsView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    private companion object {
        val GAP: PxF = 80.dpF
        val FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
    }

    private val text = Paint().apply {
        this.color = ctx.color(R.color.label_text_day)
        this.textSize = 16.dpF
    }

    private var chart: Chart = EMPTY_CHART
    private var start: IdxF = 0f
    private var end: IdxF = 0f

    fun setup(data: Chart) {
        chart = data
    }

    fun setHorizontalRange(from: IdxF, to: IdxF) {
        start = from
        end = to
        invalidate()
    }

    private fun closestPow2(x: Float): Int =
        x.log2().roundToInt().pow2()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = widthF

        val visibleIdxRange = end - start

        val daysToShow = width / GAP

        val showEveryIdx = closestPow2(visibleIdxRange / daysToShow)

        val pxPerIdx = width / visibleIdxRange

        val offset = start % showEveryIdx
        val startFromIdx = start + offset

        val x = chart.x()

        var idx = startFromIdx.toInt()
        while (idx <= end) {
            Timber.d("drawing idx $idx")
            val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(x[idx]), ZoneOffset.UTC)

            val date = FMT.format(dt)
            canvas.drawText(idx.toString(), pxPerIdx * (idx - startFromIdx), heightF / 2, text)

            idx += showEveryIdx
        }
    }
}
