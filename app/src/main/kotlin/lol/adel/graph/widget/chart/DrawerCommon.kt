package lol.adel.graph.widget.chart

import android.content.Context
import android.graphics.Paint
import help.ColorInt
import help.color
import help.dpF
import lol.adel.graph.R
import lol.adel.graph.animate
import lol.adel.graph.data.minMax
import lol.adel.graph.set
import lol.adel.graph.widget.ChartView

fun makeInnerCirclePaint(ctx: Context): Paint =
    Paint().apply {
        style = Paint.Style.FILL
        color = ctx.color(R.attr.background)
        isAntiAlias = true
    }

fun makeLinePaint(preview: Boolean, clr: ColorInt): Paint =
    Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = if (preview) 1.dpF else 2.dpF
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
        color = clr
    }

fun ChartView.initCameraAndLabels() {
    yAxis.anticipated.set(data.minMax(cameraX, enabledLines))
    yAxis.camera.set(yAxis.anticipated)
    yAxis.labels.first().set(yAxis.camera)
}

fun ChartView.animateCameraY(): Unit =
    yAxis.animate(data.minMax(cameraX, enabledLines))
