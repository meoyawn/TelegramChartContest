package lol.adel.graph.widget.chart

import android.content.Context
import android.graphics.Paint
import android.view.animation.DecelerateInterpolator
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.ChartType
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
    yAnticipated.set(data.minMax(cameraX, enabledLines))
    yCamera.set(yAnticipated)

    yLabels += YLabel.create(context).apply {
        YLabel.tune(ctx = context, label = this, isBar = data.type == ChartType.BAR)
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener {
            setAlpha(it.animatedFraction)
        }
        set(yCamera)
    }
}

fun ChartView.animateCameraY() {
    val tempY = data.minMax(cameraX, enabledLines)
    if (tempY == yAnticipated) return

    cameraMinAnim.restartWith(yCamera.min, tempY.min)
    cameraMaxAnim.restartWith(yCamera.max, tempY.max)

    if (!preview) {
        val currentYLabel = yLabels.first()
        if (tempY.distanceSq(currentYLabel) > (currentYLabel.len() * 0.2f).sq()) {

            // appear
            currentYLabel.run {
                set(tempY)
                animator.restart()
            }

            // prune
            repeat(times = yLabels.size - 3) {
                YLabel.release(yLabels[1], yLabels)
            }

            // fade
            yLabels += YLabel.obtain(ctx = context, list = yLabels, isBar = data.type == ChartType.BAR).apply {
                set(yAnticipated)
                animator.start()
            }
        }
    }
    yAnticipated.set(tempY)
}
