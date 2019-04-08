package lol.adel.graph

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import help.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId
import lol.adel.graph.data.size
import lol.adel.graph.widget.ChartView
import lol.adel.graph.widget.HorizontalLabelsView
import lol.adel.graph.widget.PreviewView
import lol.adel.graph.widget.ScrollBarView

private val ID_SCROLL = View.generateViewId()

class ViewHolder(
    val root: ViewGroup,
    val name: TextView,
    val chartView: ChartView,
    val scroll: ScrollBarView,
    val background: PreviewView,
    val horizontalLabels: HorizontalLabelsView,
    val floating: ViewGroup,
    val floatingText: TextView,
    val floatingContainer: ViewGroup,
    val dates: TextView
)

val ViewHolder.ctx: Context
    get() = root.context

fun makeChartLayout(ctx: Context, medium: Typeface, data: Chart, lineIds: List<LineId>, xs: LongArray): ViewHolder {
    lateinit var name: TextView
    lateinit var chart: ChartView
    lateinit var floating: ViewGroup
    lateinit var floatingText: TextView
    lateinit var floatingContainer: ViewGroup
    lateinit var horizintal: HorizontalLabelsView
    lateinit var background: PreviewView
    lateinit var scroll: ScrollBarView
    lateinit var dates: TextView

    val root = LinearLayout(ctx).apply {
        orientation = LinearLayout.VERTICAL

        name = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = 8.dp
                bottomMargin = 8.dp
            }
            typeface = medium
            setTextColor(ctx.color(R.color.colorAccent))
            textSize = 18f
        }
        addView(name)

        dates = TextView(ctx).apply {
            typeface = medium
        }
        addView(dates)

        val dataSize = data.size()
        val lineBuffer = FloatArray(size = dataSize.inc() * 4)

        val configuration = ctx.resources.configuration
        val height = if (configuration.screenHeightDp > configuration.screenWidthDp) 270.dp else 130.dp

        addView(FrameLayout(ctx).apply {
            layoutTransition = LayoutTransition()

            chart = ChartView(ctx, data, lineIds, lineBuffer)
            addView(chart, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            floating = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.floating_bg)
                elevation = 2.dpF
                updatePadding(left = 16.dp, top = 8.dp, right = 16.dp, bottom = 8.dp)

                floatingText = TextView(ctx).apply {
                    typeface = medium
                    setTextColor(ctx.color(R.color.floating_text))
                    textSize = 17f
                }
                addView(floatingText)

                floatingContainer = LinearLayout(ctx).apply {
                    layoutTransition = LayoutTransition()
                    orientation = LinearLayout.HORIZONTAL
                    showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                    dividerDrawable = ctx.getDrawable(R.drawable.h_space_16)
                }
                addView(floatingContainer)
            }
            addView(floating, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                topMargin = 16.dp
            })
        }, ViewGroup.LayoutParams(MATCH_PARENT, height))

        addView(FrameLayout(ctx).apply {
            horizintal = HorizontalLabelsView(ctx, xs)
            addView(horizintal)
        }, ViewGroup.LayoutParams(MATCH_PARENT, 36.dp))

        addView(FrameLayout(ctx).apply {

            addView(FrameLayout(ctx).apply {
                background = PreviewView(ctx, data, lineIds, lineBuffer)
                addView(background, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            scroll = ScrollBarView(ctx, dataSize).apply {
                id = ID_SCROLL
            }
            addView(scroll, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }, LinearLayout.LayoutParams(MATCH_PARENT, 48.dp).apply {
            bottomMargin = 10.dp
        })
    }

    return ViewHolder(
        root = root,
        name = name,
        chartView = chart,
        scroll = scroll,
        background = background,
        horizontalLabels = horizintal,
        floating = floating,
        floatingText = floatingText,
        floatingContainer = floatingContainer,
        dates = dates
    )
}
