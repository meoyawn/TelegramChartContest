package lol.adel.graph

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import help.IdxF
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId

class BackgroundChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val drawer = ChartDrawer { invalidate() }

    fun setup(chart: Chart, enabled: Set<LineId>): Unit =
        drawer.setup(chart, enabled)

    fun selectLine(id: LineId, enabled: Boolean): Unit =
        drawer.selectLine(id, enabled)

    fun setHorizontalBounds(from: IdxF, to: IdxF): Unit =
        drawer.setHorizontalBounds(from, to)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawer.onDraw(canvas)
    }
}
