package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import help.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.EMPTY_CHART
import lol.adel.graph.data.xs
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
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

    private val opaque = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
    }
    private val transparent = Paint().apply {
        color = ctx.color(R.color.label_text_day)
        textSize = 16.dpF
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
        val pxPerIdx = width / visibleIdxRange

        val rawEvery = visibleIdxRange / daysToShow
        val everyLog2 = rawEvery.log2()
        val everyFloor = everyLog2.floor().pow2()

        val everyCeil = everyLog2.ceil().pow2()
        val frac = (rawEvery - everyFloor) / (everyCeil - everyFloor)

        val startFromIdx = (start - start % everyCeil).toInt()

        val x = chart.xs()

        iterate(from = startFromIdx, to = end.ceil(), step = everyCeil) { idx ->
            val date = FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(x[idx]), ZoneOffset.UTC))
            canvas.drawText(date, pxPerIdx * (idx - start), heightF / 2, opaque)
        }

        iterate(from = startFromIdx + everyFloor, to = end.ceil(), step = everyCeil) { idx ->
            val date = FMT.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(x[idx]), ZoneOffset.UTC))
            transparent.alpha = ((1 - frac) * 255).toInt()
            canvas.drawText(date, pxPerIdx * (idx - start), heightF / 2, transparent)
        }
    }
}
