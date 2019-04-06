package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.view.View
import help.*
import lol.adel.graph.R
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ViewConstructor")
class HorizontalLabelsView(ctx: Context, private val xs: LongArray) : View(ctx) {

    companion object {
        val TEXT_SIZE_PX: PxF = 12.dpF
        private val GAP: PxF = 80.dpF
        private val PX_PER_CHAR: PxF = TEXT_SIZE_PX / 3.8f

        private val FMT = SimpleDateFormat("MMM d", Locale.US)
    }

    private val opaque = TextPaint().apply {
        color = ctx.color(R.color.label_text)
        textSize = TEXT_SIZE_PX
    }
    private val transparent = TextPaint().apply {
        color = ctx.color(R.color.label_text)
        textSize = TEXT_SIZE_PX
    }

    private var start: IdxF = 0f
    private var end: IdxF = 0f

    init {
        start = xs.size * 0.75f
        end = xs.size - 1f
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
        val hiddenEnd = end.ceil()

        iterate(from = startFromIdx, to = hiddenEnd, step = stepCeil) { idx ->
            val text = FMT.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PX_PER_CHAR), halfHeight, opaque)
        }
        transparent.alphaF = 1 - fraction
        iterate(from = startFromIdx + stepFloor, to = hiddenEnd, step = stepCeil) { idx ->
            val text = FMT.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PX_PER_CHAR), halfHeight, transparent)
        }
    }
}
