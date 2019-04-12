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

    var textSize: Float = 14f
        set(value) {
            field = value
            paints.forEachByIndex {
                it.textSize = value.dp
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

        val textHeight = newBounds.height()

        val oldY: Float = TODO()
        canvas.drawText(old, 0f, oldY, oldPaint)
        val newY: Float = TODO()
        canvas.drawText(new, 0f, newY, newPaint)
        canvas.drawText(unchanged, newBounds.width().toFloat(), heightF / 2, unchangedPaint)
    }
}
