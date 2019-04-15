package lol.adel.graph.widget.chart

import android.content.Context
import android.graphics.Paint
import help.ColorInt
import help.Idx
import help.color
import help.dpF
import lol.adel.graph.*
import lol.adel.graph.data.minMax
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
    yAxis.animate(data.minMax(cameraX, enabledLines), preview = preview)

fun fillPolyLine(points: LongArray, buf: FloatArray, cameraX: MinMax): Idx {

    var bufIdx = 0
    var first = true

    cameraX.ceilToCeil(points.size) { i ->
        val x = i.toFloat()
        val y = points[i].toFloat()

        if (first) {
            buf[bufIdx + 0] = x
            buf[bufIdx + 1] = y
            bufIdx += 2
            first = false
        } else {
            buf[bufIdx + 0] = x
            buf[bufIdx + 1] = y
            buf[bufIdx + 2] = x
            buf[bufIdx + 3] = y

            bufIdx += 4
        }
    }

    bufIdx -= 2

    return bufIdx
}
