package lol.adel.graph

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatCheckBox
import help.MATCH_PARENT
import help.WRAP_CONTENT
import help.dp
import help.updatePadding
import lol.adel.graph.data.*

private fun makeCheckbox(chart: Chart, name: ColumnName, view: ChartView): AppCompatCheckBox =
    AppCompatCheckBox(view.context).apply {
        text = chart.names[name]
        buttonTintList = ColorStateList.valueOf(chart.color(name))

        minHeight = 48.dp
        gravity = Gravity.CENTER_VERTICAL
        textSize = 16f
        isChecked = true

        setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                view.enable(name)
            } else {
                view.disable(name)
            }
        }

        val padding = 10.dp
        updatePadding(left = padding)
        layoutParams = ViewGroup.MarginLayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { leftMargin = padding }
    }

fun ViewHolder.setup(data: Chart) {
    val lines = data.lines()
    chartView.setup(data, lines)

    for (name in lines) {
        root.addView(makeCheckbox(data, name, chartView))
        root.addView(ImageView(ctx).apply { setImageResource(R.drawable.h_divider) })
    }
    root.removeViewAt(root.childCount - 1)

    scroll.listener = { left, right ->
        chartView.setHorizontalBounds(
            from = left * data.size().dec(),
            to = right * data.size().dec()
        )
    }
}
