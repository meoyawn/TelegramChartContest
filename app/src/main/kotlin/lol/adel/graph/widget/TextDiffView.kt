package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.view.View
import help.*
import lol.adel.graph.R
import lol.adel.graph.Typefaces

class TextDiffView(ctx: Context) : View(ctx) {

    var text: String = ""
        set(value) {
            if (field != value) {
                animate(from = field, to = value)
                field = value
            }
        }

    private var old: String = ""

    private var new: String = ""
        set(value) {
            if (field != value) {
                unchangedPaint.getTextBounds(value, 0, value.length, newBounds)
            }
            field = value
        }

    private var unchanged = ""

    private var frac = 1f

    private val oldPaint = TextPaint().apply { isAntiAlias = true }
    private val newPaint = TextPaint().apply { isAntiAlias = true }
    private val unchangedPaint = TextPaint().apply { isAntiAlias = true }
    private val newBounds = Rect()

    private val paints = listOf(oldPaint, newPaint, unchangedPaint)

    private val anim = ValueAnimator.ofFloat(0f, 1f).apply {
        addUpdateListener {
            frac = it.animatedFraction
            invalidate()
        }
    }

    var typeface: Typeface = Typefaces.normal
        set(value) {
            field = value
            paints.forEachByIndex {
                it.typeface = value
            }
            invalidate()
        }

    var textColor: ColorInt = ctx.color(R.attr.label_text)
        set(value) {
            field = value
            paints.forEachByIndex {
                it.color = value
            }
            invalidate()
        }

    var textSizeDp: Float = 14.dpF
        set(value) {
            field = value
            paints.forEachByIndex {
                it.textSize = value
            }
            invalidate()
        }

    private fun animate(from: String, to: String) {
        diff(from, to) { old, new, unchanged ->
            this.old = old
            this.new = new
            this.unchanged = unchanged
        }
        anim.restart()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = heightF
        val halfHeight = height / 2

        run {
            val oldFrac = 1 - frac
            val oldY = denorm(oldFrac, 0f, halfHeight)
            oldPaint.alphaF = oldFrac
            oldPaint.textSize = textSizeDp * oldFrac
            canvas.drawText(old, 0f, oldY, oldPaint)
        }

        run {
            val newY = denorm(frac, height, halfHeight)
            newPaint.alphaF = frac
            newPaint.textSize = textSizeDp * frac
            canvas.drawText(new, 0f, newY, newPaint)
        }

        canvas.drawText(unchanged, newBounds.width().toFloat(), halfHeight, unchangedPaint)
    }
}
