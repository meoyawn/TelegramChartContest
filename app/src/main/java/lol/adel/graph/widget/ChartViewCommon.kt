package lol.adel.graph.widget

import android.graphics.Paint
import help.*

fun makeLinePaint(clr: ColorInt): Paint =
    Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 2.dpF
        color = clr
    }
