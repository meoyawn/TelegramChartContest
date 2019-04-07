package lol.adel.graph

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import help.dp
import lol.adel.graph.widget.PreviewView
import lol.adel.graph.widget.ChartView
import lol.adel.graph.widget.HorizontalLabelsView
import lol.adel.graph.widget.ScrollBarView

class ViewHolder(
    val root: View,
    val linear: ViewGroup,
    val name: TextView,
    val chartView: ChartView,
    val scroll: ScrollBarView,
    val background: PreviewView,
    val horizontalLabels: HorizontalLabelsView,
    val floating: ViewGroup,
    val floatingText: TextView,
    val floatingContainer: ViewGroup,
    val bottom: View
)

val ViewHolder.ctx: Context
    get() = root.context

fun isDivider(view: View): Boolean =
    view.layoutParams.height == 1.dp
