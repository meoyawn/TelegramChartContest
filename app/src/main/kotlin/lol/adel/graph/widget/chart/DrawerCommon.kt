package lol.adel.graph.widget.chart

import android.content.Context
import android.graphics.Paint
import help.*
import lol.adel.graph.MinMax
import lol.adel.graph.R
import lol.adel.graph.animate
import lol.adel.graph.data.minMax
import lol.adel.graph.set
import lol.adel.graph.widget.ChartView
import kotlin.math.floor

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
    yAxis.animate(data.minMax(cameraX, enabledLines), preview = preview)

fun fillCurve(
    points: LongArray,
    buf: FloatArray,
    cameraX: MinMax
): Idx {
    val (start, end) = cameraX
    run {
        val i = floor(start)
        buf[0] = i
        buf[1] = points[i.toInt()].toFloat()
    }

    var bufIdx = 2

    for (i in start.ceil()..end.ceil()) {
        val x = i.toFloat()
        val y = points[i].toFloat()

        buf[bufIdx + 0] = x
        buf[bufIdx + 1] = y
        buf[bufIdx + 2] = x
        buf[bufIdx + 3] = y

        bufIdx += 4
    }

    bufIdx -= 2

    return bufIdx
}
