package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class ScrollBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    fun drawChart(canvas: Canvas) = Unit

    fun drawBars(canvas: Canvas) = Unit

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawChart(canvas)
        drawBars(canvas)
    }
}
