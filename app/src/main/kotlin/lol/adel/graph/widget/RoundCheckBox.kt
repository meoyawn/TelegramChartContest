package lol.adel.graph.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import android.widget.Checkable
import help.*
import lol.adel.graph.Interpolators
import lol.adel.graph.R
import lol.adel.graph.Typefaces

class RoundCheckBox(ctx: Context) : View(ctx), Checkable {

    override fun toggle() {
        isChecked = !isChecked
    }

    private companion object {
        val HEIGHT = 48.dp
        val LEFT_RIGHT = 26.dpF
        val V_PADDING = 4.dpF
        val CORNERS = 24.dpF
    }

    private var frac: Norm = 0f
    private val anim: ValueAnimator = ValueAnimator().apply {
        interpolator = Interpolators.DECELERATE
        addUpdateListener {
            frac = it.animatedFloat()
            invalidate()
        }
    }

    private val bgFill = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val bgStroke = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.dpF
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        textSize = 16.dpF
        color = Color.WHITE
        typeface = Typefaces.medium
        isAntiAlias = true
    }

    private val checkD = ctx.getDrawable(R.drawable.check)!!.mutate()

    private var textLen: X = -1f

    var text: String = ""
        set(value) {
            field = value
            textLen = textPaint.measureText(text)
        }

    var color: ColorInt = Color.BLACK

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int): Unit =
        setMeasuredDimension((textLen + LEFT_RIGHT * 2).toInt(), HEIGHT)

    private var checked: Boolean = false

    override fun setChecked(checked: Boolean) {
        this.checked = checked
        val targetFrac = if (checked) 1f else 0f
        if (width > 0) {
            anim.restartWith(frac, targetFrac)
        } else {
            frac = targetFrac
        }
    }

    override fun isChecked(): Boolean =
        checked

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val h = heightF
        val w = widthF

        bgFill.color = argb(frac, Color.TRANSPARENT, color)
        bgStroke.color = argb(frac, color, Color.TRANSPARENT)
        textPaint.color = argb(frac, color, Color.WHITE)

        canvas.drawRoundRect(2.dpF, V_PADDING, w - 2.dpF, h - V_PADDING, CORNERS, CORNERS, bgFill)
        canvas.drawRoundRect(2.dpF, V_PADDING, w - 2.dpF, h - V_PADDING, CORNERS, CORNERS, bgStroke)

        val checkY = denorm(frac, 24.dp, 12.dp)
        val checkLeft = denorm(frac, 22.dp, 10.dp)
        val checkSize = 24.dpF * frac

        checkD.setBounds(checkLeft, checkY, (checkLeft + checkSize).toInt(), (checkSize + checkY).toInt())
        checkD.draw(canvas)

        val checkLen = denorm(frac, LEFT_RIGHT, 34.dpF)
        canvas.drawText(text, checkLen, h / 2 + textPaint.descent() + 2.dpF, textPaint)
    }
}
