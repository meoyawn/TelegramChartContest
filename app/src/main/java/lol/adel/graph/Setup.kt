package lol.adel.graph

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import lol.adel.graph.widget.ChartView
import lol.adel.graph.widget.ScrollBarView
import java.text.SimpleDateFormat
import java.util.*

private fun makeCheckbox(
    chart: Chart,
    id: LineId,
    viewHolder: ViewHolder,
    texts: SimpleArrayMap<LineId, ViewGroup>
): CheckBox {
    val ctx = viewHolder.ctx

    return CheckBox(ctx).apply {
        text = chart.names[id]
        buttonTintList = ColorStateList.valueOf(chart.color(id))

        minHeight = 48.dp
        gravity = Gravity.CENTER_VERTICAL
        textSize = 18f
        isChecked = true

        setOnCheckedChangeListener { _, isChecked ->
            viewHolder.chartView.selectLine(id, isChecked)
            viewHolder.background.selectLine(id, isChecked)
            texts[id]?.visibility = visibleOrGone(isChecked)
        }

        updatePadding(left = 10.dp)
    }
}

private fun makeLineText(ctx: Context, chart: Chart, id: LineId, medium: Typeface): ViewGroup =
    LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL

        val color = chart.color(id)

        addView(TextView(ctx).apply {
            textSize = 18f
            setTextColor(color)
            typeface = medium
        })

        addView(TextView(ctx).apply {
            textSize = 16f
            setTextColor(color)
            text = chart.names[id]
        })
    }

object Typefaces {
    val medium: Typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
}

fun ViewHolder.setup(idx: Idx, data: Chart, lineIds: Set<LineId>, xs: LongArray) {
    name.text = chartName(idx)

    val lineTexts = simpleArrayMapOf<LineId, ViewGroup>()
    for (id in lineIds) {
        linear.addView(makeCheckbox(data, id, this, lineTexts))
        linear.addView(
            View(ctx).apply { setBackgroundResource(R.drawable.h_divider) },
            ViewGroup.LayoutParams(MATCH_PARENT, 1.dp)
        )

        val text = makeLineText(ctx, data, id, Typefaces.medium)
        floatingContainer.addView(text)
        lineTexts[id] = text
    }
    linear.removeViewAt(linear.childCount - 1)

    val size = data.size()
    val lastIndex = size - 1
    background.setHorizontalBounds(from = 0f, to = lastIndex.toFloat())

    scroll.listener = object : ScrollBarView.Listener {
        override fun onBoundsChange(left: Float, right: Float) {
            val start = left * lastIndex
            val end = right * lastIndex
            chartView.setHorizontalBounds(from = start, to = end)
            horizontalLabels.setHorizontalRange(from = start, to = end)
        }
    }

    val fmt = SimpleDateFormat("EEE, MMM d", Locale.US)

    floating.visibility = View.INVISIBLE
    chartView.listener = object : ChartView.Listener {
        override fun onTouch(idx: Idx, x: PxF, maxY: Float) {
            floating.visibility = visibleOrInvisible(idx != -1)

            if (idx in 0..lastIndex) {
                val floatingWidth = floating.width

                val target = x - 20.dp
                val altTarget = x - floatingWidth + 40.dp

                val rightOk = target + floatingWidth <= chartView.width

                floating.translationX = when {
                    target > 0 && rightOk ->
                        target

                    !rightOk && altTarget > 0 ->
                        altTarget

                    else ->
                        0f
                }

                floatingText.text = fmt.format(xs[idx])

                lineTexts.forEach { id, view ->
                    val points = data.columns[id]!!
                    view.component1().toTextView().text = chartValue(points[idx], maxY)
                }
            }
        }
    }
}
