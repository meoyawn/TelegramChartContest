package lol.adel.graph

import android.content.Context
import android.view.ViewGroup

class ViewHolder(
    val root: ViewGroup,
    val chartView: ChartView,
    val scroll: ScrollBarView,
    val background: BackgroundChartView,
    val horizontalLabels: HorizontalLabelsView
)

val ViewHolder.ctx: Context
    get() = root.context
