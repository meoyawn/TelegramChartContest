package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
import lol.adel.graph.get
import lol.adel.graph.len
import lol.adel.graph.norm
import lol.adel.graph.widget.ChartView

class BarDrawer(val view: ChartView) : TypeDrawer {

    override fun makePaint(clr: ColorInt): Paint =
        Paint().apply {
            style = Paint.Style.STROKE
            color = clr
        }

    override fun draw(canvas: Canvas) {
        val (start, end) = view.cameraX
        val cameraY = view.cameraY
        val height = view.heightF
        val eHeight = view.effectiveHeight()
        val width = view.widthF

        var x = view.mapX(start.floor(), width)
        val bw = width / view.cameraX.len()

        for (i in start.floor()..end.ceil()) {
            var y: PxF = height
            view.animatedColumns.forEach { _, column ->
                if (column.frac > 0) {
                    val bh = cameraY.norm(column[i]) * eHeight
                    val paint = column.paint
                    canvas.drawRect(x, y - bh, x + bw, y, paint)
                    y -= bh
                }
            }
            x += bw
        }

        // TODO touching fade

        view.drawYLines(height, canvas, width)
    }
}
