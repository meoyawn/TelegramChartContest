package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun drawLines(canvas: Canvas) = Unit
    fun drawScales(canvas: Canvas) = Unit

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLines(canvas)
        drawScales(canvas)
    }
}
