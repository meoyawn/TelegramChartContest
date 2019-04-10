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
    private lateinit var details: DetailsView
    private lateinit var xLabels: XLabelsView
    private lateinit var preview: ChartView
    private lateinit var scroll: ScrollBarView
    private lateinit var dates: TextView

    // state
    private val enabledLines = ArrayList(data.lineIds)
    private val cameraX = MinMax(min = 0f, max = data.size - 1f)

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
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable("super"))
            cameraX.set(state.getFloat("start"), state.getFloat("end"))
            state.getStringArrayList("enabled")?.let {
                enabledLines.clear()
                enabledLines.addAll(it)
            }
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

            chartView = ChartView(ctx, data, lineBuffer, cameraX, enabledLines, false)
            addView(chartView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            details = DetailsView(ctx, data, enabledLines)
            addView(details, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { topMargin = 28.dp })
        }, LinearLayout.LayoutParams(MATCH_PARENT, height))

        addView(FrameLayout(ctx).apply {
            xLabels = XLabelsView(ctx, data.xs, cameraX)
            addView(xLabels)
        }, LinearLayout.LayoutParams(MATCH_PARENT, 36.dp))

        val lastIndex = data.size - 1

        addView(FrameLayout(ctx).apply {
            val previewCamX = MinMax(min = 0f, max = lastIndex.toFloat())
            preview = ChartView(ctx, data, lineBuffer, previewCamX, enabledLines, preview = true)
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
                    details.lineChecked(select, deselect)
                }
            }
        })

        val range = Dates.HEADER_RANGE
        dates.text =
            "${range.format(data.xs[cameraX.min.toInt()])} - ${range.format(data.xs[cameraX.max.toInt()])}"

        scroll.listener = object : ScrollBarView.Listener {
            override fun onBoundsChange(left: Float, right: Float) {

                cameraX.set(left * lastIndex, right * lastIndex)
                chartView.cameraXChanged()
                xLabels.cameraXChanged()


                dates.text =
                    "${range.format(data.xs[cameraX.min.toInt()])} - ${range.format(data.xs[cameraX.max.toInt()])}"
            }
        }

        details.visibility = View.INVISIBLE
        chartView.listener = object : ChartView.Listener {
            override fun onTouch(idx: Idx, x: PxF) {
                details.visibility = visibleOrInvisible(idx != -1)

                if (idx in 0..lastIndex) {
                    details.show(idx, x)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (width > 0) {
            setMeasuredDimension(width, height)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed) {
            super.onLayout(changed, l, t, r, b)
        }
    }
}
