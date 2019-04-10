package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.ColorInt

interface TypeDrawer {

    fun makePaint(clr: ColorInt): Paint

    fun draw(canvas: Canvas)
}
