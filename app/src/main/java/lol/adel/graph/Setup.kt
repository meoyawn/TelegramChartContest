package lol.adel.graph

import android.content.res.ColorStateList
import android.view.Gravity
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckBox
import help.Idx
import help.dp
import help.updatePadding
import lol.adel.graph.data.*

private fun makeCheckbox(chart: Chart, id: LineId, viewHolder: ViewHolder): AppCompatCheckBox =
    AppCompatCheckBox(viewHolder.ctx).apply {
        text = chart.names[id]
        buttonTintList = ColorStateList.valueOf(chart.color(id))

        minHeight = 48.dp
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
        isChecked = true

        setOnCheckedChangeListener { _, isChecked ->
            viewHolder.chartView.selectLine(id, isChecked)
            viewHolder.background.selectLine(id, isChecked)
        }

        updatePadding(left = 10.dp)
    }

fun ViewHolder.setup(idx: Idx) {
    name.text = ctx.getString(R.string.chart_d, idx)
    val data = CHARTS[idx]

    val lines = data.lines()
    chartView.setup(data, lines)
    background.setup(data, lines)
    horizontalLabels.setup(data)

    for (name in lines) {
        root.addView(makeCheckbox(data, name, this))
        root.addView(ImageView(ctx).apply { setImageResource(R.drawable.h_divider) })
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
}
