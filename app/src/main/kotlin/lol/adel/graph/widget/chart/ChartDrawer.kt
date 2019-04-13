package lol.adel.graph.widget.chart

import android.graphics.Canvas
import android.graphics.Paint
import help.*
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

    fun labelColor(): ColorInt =
        view.color(R.attr.label_text)

    fun maxLabelAlpha(): Norm =
        1f

    fun verticalSplits(): Int =
        5

    fun touch(idx: IdxF, x: X) = Unit
    fun touchUp() = Unit
    fun touchClear() = Unit
}
