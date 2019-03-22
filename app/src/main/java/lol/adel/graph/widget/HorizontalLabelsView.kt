package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import help.*
import lol.adel.graph.R
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ViewConstructor")
class HorizontalLabelsView(ctx: Context, private val xs: LongArray) : View(ctx) {

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

    fun toggleNight(): Unit =
        animateColor(opaque, transparent, R.color.label_text)

    private var start: IdxF = 0f
    private var end: IdxF = 0f

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
