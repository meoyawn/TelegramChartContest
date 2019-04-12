package lol.adel.graph.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import android.view.animation.DecelerateInterpolator
import help.*
import lol.adel.graph.R
import lol.adel.graph.Typefaces

class TextDiffView(ctx: Context) : View(ctx) {

    private val newBounds = Rect()

    private val oldPaint = TextPaint().apply { isAntiAlias = true }
    private val newPaint = TextPaint().apply { isAntiAlias = true }
    private val unchangedPaint = TextPaint().apply { isAntiAlias = true }

    private var frac = 1f
    private val anim = valueAnimator().apply {
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            frac = it.animatedFraction
            invalidate()
        }
        duration = 200
    }

    private var splitIdx: Idx = 0
    private var prevText: String = ""

    var text: String = ""
        set(value) {
            if (field != value) {
                splitIdx = if (fullFlip) 0 else firstChangeFromEnd(field, value)
                unchangedPaint.getTextBounds(value, 0, value.length - splitIdx, newBounds)
                prevText = field

                field = value

                anim.restart()
            }
        }

    private val paints = listOf(oldPaint, newPaint, unchangedPaint)

    var fullFlip: Boolean = false

    var typeface: Typeface = Typefaces.normal
        set(value) {
            field = value
            paints.forEachByIndex { it.typeface = value }
            invalidate()
        }

    var textColor: ColorInt = ctx.color(R.attr.label_text)
        set(value) {
            field = value
            paints.forEachByIndex { it.color = value }
            invalidate()
        }

    var textSizeDp: Float = 14.dpF
        set(value) {
            field = value
            paints.forEachByIndex { it.textSize = value }
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = heightF

        val blankX = newBounds.width().toFloat()
        val halfBlankX = blankX / 2

        val textLen = text.length
        canvas.drawText(text, textLen - splitIdx, textLen, blankX, height / 2, unchangedPaint)

        run {
            val oldFrac = 1 - frac

            oldPaint.alphaF = oldFrac
            oldPaint.textSize = textSizeDp * oldFrac

            val x = denorm(oldFrac, halfBlankX, 0f)
            val y = denorm(frac, height / 2, 0f)

            canvas.drawText(prevText, 0, prevText.length - splitIdx, x, y, oldPaint)
        }

        run {
            newPaint.alphaF = frac
            newPaint.textSize = textSizeDp * frac

            val x = denorm(frac, halfBlankX, 0f)
            val y = denorm(frac, height, height / 2)

            canvas.drawText(text, 0, textLen - splitIdx, x, y, newPaint)
        }
    }
}
