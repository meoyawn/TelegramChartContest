package lol.adel.graph

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import help.IdxF
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId

class ChartView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(ctx, attrs, defStyleAttr) {

    private val charter = ChartDrawer(ctx = ctx, drawLabels = true) { invalidate() }

    fun setup(chart: Chart, enabled: Set<LineId>): Unit =
        charter.setup(chart, enabled)

    fun selectLine(id: LineId, enabled: Boolean): Unit =
        charter.selectLine(id, enabled)

    fun setHorizontalBounds(from: IdxF, to: IdxF): Unit =
        charter.setHorizontalBounds(from, to)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                charter.touching = event.x
            }

            MotionEvent.ACTION_MOVE -> {
                charter.touching = event.x
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                charter.touching = -1f
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        charter.onDraw(canvas)
    }
}
