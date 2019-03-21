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
import java.text.SimpleDateFormat
import java.util.*

class HorizontalLabelsView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    companion object {
        private val format = SimpleDateFormat("MMM d", Locale.US)
        private val GAP = 80.dpF
        private val PER_CHAR = 4.3f.dp
    }

    private val opaque = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = 16.dpF
        isAntiAlias = true
    }
    private val transparent = Paint().apply {
        color = ctx.color(R.color.label_text)
        textSize = 16.dpF
        isAntiAlias = true
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

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = widthF
        val halfHeight = heightF / 2

        val visibleIdxRange = end - start
        val daysToShow = width / GAP
        val pxPerIdx = width / visibleIdxRange

        val rawStep = visibleIdxRange / daysToShow
        val everyLog2 = rawStep.log2()
        val stepFloor = everyLog2.floor().pow2()
        val stepCeil = everyLog2.ceil().pow2()

        val fraction = if (stepCeil == stepFloor) 1f
        else (rawStep - stepFloor) / (stepCeil - stepFloor)

        val startFromIdx = (start - start % stepCeil).toInt()
        val xs = chart.xs()
        val hiddenEnd = end.ceil()

        iterate(from = startFromIdx, to = hiddenEnd, step = stepCeil) { idx ->
            val text = format.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PER_CHAR), halfHeight, opaque)
        }
        transparent.alphaF = 1 - fraction
        iterate(from = startFromIdx + stepFloor, to = hiddenEnd, step = stepCeil) { idx ->
            val text = format.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PER_CHAR), halfHeight, transparent)
        }
    }
}
