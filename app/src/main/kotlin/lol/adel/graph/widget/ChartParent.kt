package lol.adel.graph.widget

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId

@SuppressLint("ViewConstructor")
class ChartParent(
    ctx: Context,
    val data: Chart,
    private val idx: Idx,
    private val lineBuffer: FloatArray
) : LinearLayout(ctx) {

    private companion object {
        val CHART_NAMES = listOf("Followers", "Interactions", "Messages", "Views", "Apps")
        val ID = View.generateViewId()
    }

    private lateinit var name: TextView
    private lateinit var chartView: ChartView
    private lateinit var toolTip: ToolTipView
    private lateinit var xLabels: XLabelsView
    private lateinit var preview: ChartView
    private lateinit var scroll: ScrollBarView
    private lateinit var dates: TextView

    // state
    private val enabledLines = ArrayList(data.lineIds)
    private val cameraX = MinMax(min = 0f, max = data.size - 1f)

    private val touchingIdx = MutableInt(get = -1)
    private val touchingX = MutableFloat(get = -1f)

    init {
        id = ID + idx
        orientation = LinearLayout.VERTICAL
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putParcelable("super", super.onSaveInstanceState())
            putFloat("start", cameraX.min)
            putFloat("end", cameraX.max)
            putStringArrayList("enabled", ArrayList(enabledLines))

            putInt("idx", touchingIdx.get)
            putFloat("x", touchingX.get)
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("super"))
            cameraX.set(state.getFloat("start"), state.getFloat("end"))
            state.getStringArrayList("enabled")?.let {
                enabledLines.clear()
                enabledLines.addAll(it)
            }

            touchingIdx.get = state.getInt("idx")
            touchingX.get = state.getFloat("x")
            println(touchingX.toString())
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val ctx = context

        addView(FrameLayout(ctx).apply {
            name = TextView(ctx).apply {
                text = CHART_NAMES[idx]
                typeface = Typefaces.bold
                setTextColor(ctx.color(R.attr.floating_text))
                textSize = 16f
                gravity = Gravity.CENTER_VERTICAL
            }
            addView(name, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            dates = TextView(ctx).apply {
                typeface = Typefaces.bold
                setTextColor(ctx.color(R.attr.floating_text))
                textSize = 12f
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
            addView(dates, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }, LinearLayout.LayoutParams(MATCH_PARENT, 24.dp).apply {
            topMargin = 8.dp
            bottomMargin = 4.dp

            leftMargin = 16.dp
            rightMargin = 16.dp
        })

        val configuration = ctx.resources.configuration
        val height = if (configuration.screenHeightDp > configuration.screenWidthDp) 260.dp else 130.dp

        addView(FrameLayout(ctx).apply {
            layoutTransition = LayoutTransition()

            chartView = ChartView(ctx, data, lineBuffer, cameraX, enabledLines, false, touchingIdx, touchingX)
            addView(chartView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            toolTip = ToolTipView(ctx, data, enabledLines, touchingIdx)
            addView(toolTip, FrameLayout.LayoutParams(140.dp, WRAP_CONTENT).apply { topMargin = 28.dp })
        }, LinearLayout.LayoutParams(MATCH_PARENT, height))

        addView(FrameLayout(ctx).apply {
            xLabels = XLabelsView(ctx, data.xs, cameraX)
            addView(xLabels)
        }, LinearLayout.LayoutParams(MATCH_PARENT, 36.dp))

        val lastIndex = data.size - 1

        addView(FrameLayout(ctx).apply {
            val previewCamX = MinMax(min = 0f, max = lastIndex.toFloat())
            preview = ChartView(ctx, data, lineBuffer, previewCamX, enabledLines, true, touchingIdx, touchingX)
            addView(preview, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            scroll = ScrollBarView(ctx, cameraX, data.size)
            addView(scroll, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }, LinearLayout.LayoutParams(MATCH_PARENT, 48.dp).apply {
            bottomMargin = 10.dp
        })

        addView(FilterView(ctx, data, enabledLines).apply {
            listener = object : FilterView.Listener {
                override fun onChange(select: List<LineId>, deselect: List<LineId>) {
                    enabledLines.removeAll(deselect)
                    enabledLines.addAll(select)

                    chartView.lineSelected(select, deselect)
                    preview.lineSelected(select, deselect)
                    toolTip.lineChecked(select, deselect)
                }
            }
        })

        dates.text = currentDateRange()
        scroll.listener = object : ScrollBarView.Listener {
            override fun onBoundsChange(left: Float, right: Float) {
                cameraX.set(left * lastIndex, right * lastIndex)
                chartView.cameraXChanged()
                xLabels.cameraXChanged()
                dates.text = currentDateRange()
            }
        }

        touchChange()
        chartView.listener = object : ChartView.Listener {
            override fun onTouch(idx: Idx, x: PxF) {
                touchingIdx.get = idx
                touchingX.get = x
                touchChange()
            }
        }
    }

    private fun touchChange() {
        println(touchingX.toString())

        toolTip.visibility = visibleOrInvisible(touchingIdx.get != -1)
        toolTip.show(touchingIdx.get, touchingX.get)
    }

    private fun currentDateRange(): String {
        val min = 0
        val max = data.size - 1

        val left = Dates.header(data.xs[clamp(cameraX.min.toInt(), min, max)])
        val right = Dates.header(data.xs[clamp(cameraX.max.toInt(), min, max)])
        return "$left - $right"
    }
}
