package lol.adel.graph

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
import lol.adel.graph.widget.BackgroundChartView
import lol.adel.graph.widget.ChartView
import lol.adel.graph.widget.HorizontalLabelsView
import lol.adel.graph.widget.ScrollBarView

private val scrollId = View.generateViewId()

fun makeChartLayout(ctx: Context, medium: Typeface, data: Chart, lineIds: Set<LineId>): ViewHolder {
    lateinit var linear: LinearLayout
    lateinit var name: TextView
    lateinit var chart: ChartView
    lateinit var floating: ViewGroup
    lateinit var floatingText: TextView
    lateinit var floatingContainer: ViewGroup
    lateinit var horizintal: HorizontalLabelsView
    lateinit var background: BackgroundChartView
    lateinit var scroll: ScrollBarView

    val root = LinearLayout(ctx).apply {
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        orientation = LinearLayout.VERTICAL

        linear = LinearLayout(ctx).apply {
            updatePadding(left = 20.dp, right = 20.dp)
            clipToPadding = false
            orientation = LinearLayout.VERTICAL
            clipChildren = false

            name = TextView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    topMargin = 16.dp
                    bottomMargin = 8.dp
                }
                typeface = medium
                setTextColor(ctx.color(R.color.colorAccent))
                textSize = 18f
            }
            addView(name)

            addView(FrameLayout(ctx).apply {
                clipChildren = true

                chart = ChartView(ctx, data, lineIds)
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
                        orientation = LinearLayout.HORIZONTAL
                        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                        dividerDrawable = ctx.getDrawable(R.drawable.h_space_16)
                    }
                    addView(floatingContainer)
                }
                addView(floating, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    topMargin = 10.dp
                })
            }, ViewGroup.LayoutParams(MATCH_PARENT, 270.dp))

            horizintal = HorizontalLabelsView(ctx)
            addView(horizintal, ViewGroup.LayoutParams(MATCH_PARENT, 36.dp))

            addView(FrameLayout(ctx).apply {
                clipChildren = false

                addView(FrameLayout(ctx).apply {
                    background = BackgroundChartView(ctx)
                    addView(background, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

                scroll = ScrollBarView(ctx).apply {
                    id = scrollId
                }
                addView(scroll, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }, LinearLayout.LayoutParams(MATCH_PARENT, 48.dp).apply {
                bottomMargin = 10.dp
            })
        }
        addView(linear, LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        addView(View(ctx).apply {
            setBackgroundResource(R.color.bottom)
        }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    return ViewHolder(
        root = root,
        linear = linear,
        name = name,
        chartView = chart,
        floatingText = floatingText,
        floating = floating,
        floatingContainer = floatingContainer,
        horizontalLabels = horizintal,
        background = background,
        scroll = scroll
    )
}
