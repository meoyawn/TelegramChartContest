package lol.adel.graph

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.*
import java.text.SimpleDateFormat
import java.util.*

private fun makeCheckbox(
    chart: Chart,
    id: LineId,
    viewHolder: ViewHolder,
    texts: SimpleArrayMap<LineId, ViewGroup>
): AppCompatCheckBox =
    AppCompatCheckBox(viewHolder.ctx).apply {
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

private fun makeLineText(ctx: Context, chart: Chart, id: LineId, medium: Typeface): ViewGroup =
    LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL

        val color = chart.color(id)

        addView(AppCompatTextView(ctx).apply {
            textSize = 19f
            setTextColor(color)
            typeface = medium
        })

        addView(AppCompatTextView(ctx).apply {
            textSize = 17f
            setTextColor(color)
            text = chart.names[id]
        })
    }

fun ViewHolder.setup(idx: Idx) {
    name.text = ctx.getString(R.string.chart_d, idx)
    val data = CHARTS[idx]

    val lines = data.lines()
    chartView.setup(data, lines)
    background.setup(data, lines)
    horizontalLabels.setup(data)

    val medium = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    val lineTexts = simpleArrayMapOf<LineId, ViewGroup>()
    for (id in lines) {
        root.addView(makeCheckbox(data, id, this, lineTexts))
        root.addView(ImageView(ctx).apply { setImageResource(R.drawable.h_divider) })

        val text = makeLineText(ctx, data, id, medium)
        floatingContainer.addView(text)
        lineTexts[id] = text
    }
    root.removeViewAt(root.childCount - 1)

    val size = data.size()
    background.setHorizontalBounds(from = 0f, to = size - 1f)

    scroll.listener = object : ScrollBarView.Listener {
        override fun onBoundsChange(left: Float, right: Float) {
            chartView.setHorizontalBounds(
                from = left * size.dec(),
                to = right * size.dec()
            )
            horizontalLabels.setHorizontalRange(
                from = left * size.dec(),
                to = right * size.dec()
            )
        }
    }

    val fmt = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val xs = data.xs()

    floating.visibility = View.INVISIBLE
    chartView.listener = object : ChartView.Listener {
        override fun onTouch(idx: Idx, x: PxF) {
            floating.visibility = visibleOrInvisible(idx != -1)

            if (idx in 0..(size - 1)) {
                val floatingWidth = floating.width

                val target = x - 20.dp
                val altTarget = x - floatingWidth + 20.dp

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
                    view.component1().toTextView().text = points[idx].toString()
                }
            }
        }
    }
}
