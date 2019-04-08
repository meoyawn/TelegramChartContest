package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.text.TextPaint
import android.view.View
import help.*
import lol.adel.graph.Dates
import lol.adel.graph.MinMax
import lol.adel.graph.R
import lol.adel.graph.len

@SuppressLint("ViewConstructor")
class XLabelsView(ctx: Context, private val xs: LongArray, val cameraX: MinMax) : View(ctx) {

    companion object {
        val TEXT_SIZE_PX: PxF = 12.dpF
        private val GAP: PxF = 80.dpF
        private val PX_PER_CHAR: PxF = TEXT_SIZE_PX / 3.8f
    }

    private val opaque = TextPaint().apply {
        color = ctx.color(R.color.label_text)
        textSize = TEXT_SIZE_PX
    }
    private val transparent = TextPaint().apply {
        color = ctx.color(R.color.label_text)
        textSize = TEXT_SIZE_PX
    }

    fun cameraXChanged() {
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = widthF
        val halfHeight = heightF / 2

        val visibleIdxRange = cameraX.len()
        val daysToShow = width / GAP
        val pxPerIdx = width / visibleIdxRange

        val rawStep = visibleIdxRange / daysToShow
        val everyLog2 = rawStep.log2()
        val stepFloor = everyLog2.floor().pow2()
        val stepCeil = everyLog2.ceil().pow2()

        val fraction = if (stepCeil == stepFloor) 1f
        else (rawStep - stepFloor) / (stepCeil - stepFloor)

        val (start, end) = cameraX
        val startFromIdx = (start - start % stepCeil).toInt()
        val hiddenEnd = end.ceil()

        val format = Dates.HORIZONTAL
        iterate(from = startFromIdx, to = hiddenEnd, step = stepCeil) { idx ->
            val text = format.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PX_PER_CHAR), halfHeight, opaque)
        }
        transparent.alphaF = 1 - fraction
        iterate(from = startFromIdx + stepFloor, to = hiddenEnd, step = stepCeil) { idx ->
            val text = format.format(xs[idx])
            canvas.drawText(text, pxPerIdx * (idx - start) - (text.length * PX_PER_CHAR), halfHeight, transparent)
        }
    }
}
