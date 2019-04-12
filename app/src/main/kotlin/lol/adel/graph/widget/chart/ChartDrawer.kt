package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.ColorInt
import help.Norm
import help.Px
import help.color
import lol.adel.graph.R
import lol.adel.graph.widget.ChartView

interface ChartDrawer {

    val view: ChartView

    fun initYAxis(): Unit =
        view.initCameraAndLabels()

    fun makePaint(clr: ColorInt): Paint

    fun draw(canvas: Canvas)

    fun animateYAxis(): Unit =
        view.animateCameraY()

    fun bottomOffset(): Px =
        0

    fun labelColor(): ColorInt =
        view.color(R.attr.label_text)

    fun maxLabelAlpha(): Norm =
        1f

    fun verticalSplits(): Int =
        5
}
