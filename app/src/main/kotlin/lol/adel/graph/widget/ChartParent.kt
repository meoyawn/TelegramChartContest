package lol.adel.graph.widget

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
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
class ChartParent(ctx: Context, val data: Chart, val idx: Idx) : LinearLayout(ctx) {

    private companion object {
        val NAMES = listOf("Followers", "Interactions", "Growth", "Messages", "Views", "Apps")
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
    private val cameraX = MinMax(min = data.size * 0.75f, max = data.size - 1f)

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

        name = TextView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                topMargin = 8.dp
                bottomMargin = 8.dp
            }
            text = NAMES[idx]
            typeface = Typefaces.medium
            setTextColor(ctx.color(R.color.colorAccent))
            textSize = 18f
        }
        addView(name)

        dates = TextView(ctx).apply {
            typeface = Typefaces.medium
        }
        addView(dates)

        val lineBuffer = FloatArray(size = data.size.inc() * 4)
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

        scroll.listener = object : ScrollBarView.Listener {
            override fun onBoundsChange(left: Float, right: Float) {

                cameraX.set(left * lastIndex, right * lastIndex)
                chartView.cameraXChanged()
                xLabels.cameraXChanged()

                val range = Dates.HEADER_RANGE
                dates.text =
                    "${range.format(data.xs[(left * lastIndex).toInt()])} - ${range.format(data.xs[(right * lastIndex).toInt()])}"
            }
        }

        details.visibility = View.INVISIBLE
        chartView.listener = object : ChartView.Listener {
            override fun onTouch(idx: Idx, x: PxF) {
                details.visibility = visibleOrInvisible(idx != -1)

                if (idx in 0..lastIndex) {
                    val floatingWidth = details.width

                    val target = x - 20.dp
                    val altTarget = x - floatingWidth + 40.dp

                    val rightOk = target + floatingWidth <= chartView.width

                    details.translationX = when {
                        target > 0 && rightOk ->
                            target

                        !rightOk && altTarget > 0 ->
                            altTarget

                        else ->
                            0f
                    }
                    details.redraw(idx)
                }
            }
        }
    }
}
