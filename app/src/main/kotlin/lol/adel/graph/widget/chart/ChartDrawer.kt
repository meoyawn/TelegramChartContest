package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.ColorInt
import lol.adel.graph.widget.ChartView

interface ChartDrawer {

    val view: ChartView

    fun initYAxis(): Unit =
        view.initCameraAndLabels()

    fun makePaint(clr: ColorInt): Paint

    fun draw(canvas: Canvas)

    fun animateYAxis(): Unit =
        view.animateCameraY()
}
