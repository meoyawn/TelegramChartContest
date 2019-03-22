package lol.adel.graph

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import lol.adel.graph.widget.BackgroundChartView
import lol.adel.graph.widget.ChartView
import lol.adel.graph.widget.HorizontalLabelsView
import lol.adel.graph.widget.ScrollBarView

class ViewHolder(
    val root: View,
    val linear: ViewGroup,
    val name: TextView,
    val chartView: ChartView,
    val scroll: ScrollBarView,
    val background: BackgroundChartView,
    val horizontalLabels: HorizontalLabelsView,
    val floating: View,
    val floatingText: TextView,
    val floatingContainer: ViewGroup
)

val ViewHolder.ctx: Context
    get() = root.context
