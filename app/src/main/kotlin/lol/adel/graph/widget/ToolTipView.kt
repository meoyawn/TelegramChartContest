package lol.adel.graph.widget

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.Dates
import lol.adel.graph.R
import lol.adel.graph.Typefaces
import lol.adel.graph.data.*
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class ToolTipView(
    ctx: Context,
    private val data: Chart,
    private val enabledLines: List<LineId>,
    private val touchingIdx: MutableInt
) : LinearLayout(ctx) {

    private companion object {
        fun ViewGroup.percentage(): TextDiffView =
            getChildAt(0) as TextDiffView

        fun ViewGroup.value(): TextDiffView =
            getChildAt(2) as TextDiffView
    }

    private val floatingText: TextDiffView
    private val floatingContainer: ViewGroup

    private val lineTexts = SimpleArrayMap<LineId, ViewGroup>()
    private val all: TextDiffView

    private fun makeLineText(ctx: Context, chart: Chart, id: LineId): ViewGroup =
        LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL

            visibility = visibleOrGone(id in enabledLines)

            addView(TextDiffView(ctx).apply {
                visibility = visibleOrGone(chart.percentage)
                textSizeDp = 12.dpF
                textColor = ctx.color(R.attr.floating_text)
                typeface = Typefaces.bold
                gravity = Gravity.END
            }, LinearLayout.LayoutParams(30.dp, 20.dp).apply {
                marginEnd = 4.dp
            })

            addView(TextView(ctx).apply {
                textSize = 12f
                setTextColor(ctx.color(R.attr.floating_text))
                text = chart.names[id]
            })

            addView(TextDiffView(ctx).apply {
                textSizeDp = 12.dpF
                textColor = chart.color(id)
                typeface = Typefaces.bold
                fullFlip = true
                gravity = Gravity.END
            }, LayoutParams(MATCH_PARENT, 20.dp))
        }

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.floating_bg)
        elevation = 2.dpF
        updatePadding(left = 10.dp, top = 6.dp, right = 10.dp, bottom = 6.dp)

        addView(FrameLayout(ctx).apply {
            floatingText = TextDiffView(ctx).apply {
                typeface = Typefaces.bold
                textColor = ctx.color(R.attr.floating_text)
                textSizeDp = 12.dpF
                gravity = Gravity.START
            }
            addView(floatingText)

            addView(ImageView(ctx).apply {
                setImageResource(R.drawable.arrow)
            }, FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                topMargin = 4.dp
                gravity = Gravity.END
            })
        }, LinearLayout.LayoutParams(MATCH_PARENT, 20.dp))

        floatingContainer = LinearLayout(ctx).apply {
            layoutTransition = LayoutTransition()
            orientation = LinearLayout.VERTICAL
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerDrawable = ctx.getDrawable(R.drawable.h_space_16)
        }
        addView(floatingContainer)

        data.lineIds.forEachByIndex { id ->
            val text = makeLineText(ctx, data, id)
            floatingContainer.addView(text)
            lineTexts[id] = text
        }

        floatingContainer.addView(LinearLayout(ctx).apply {
            visibility = visibleOrGone(data.stacked && !data.percentage)

            addView(TextView(ctx).apply {
                textSize = 12f
                setTextColor(ctx.color(R.attr.floating_text))
                setText(R.string.all)
            })

            all = TextDiffView(ctx).apply {
                textSizeDp = 12.dpF
                textColor = ctx.color(R.attr.floating_text)
                typeface = Typefaces.bold
                fullFlip = true
                gravity = Gravity.END
            }
            addView(all, LayoutParams(MATCH_PARENT, 20.dp))
        })

        setOnClickListener {
            Toast.makeText(ctx, "Not implemented", Toast.LENGTH_SHORT).show()
        }
    }

    fun lineChecked(select: List<LineId>, deselect: List<LineId>) {
        deselect.forEachByIndex {
            lineTexts[it]?.visibility = View.GONE
        }
        select.forEachByIndex {
            lineTexts[it]?.visibility = View.VISIBLE
        }

        if (touchingIdx.get != -1) {
            updateValues(touchingIdx.get)
        }
    }

    fun show(idx: Idx, x: PxF) {
        if (idx == -1) return

        whenMeasured {
            val floatingWidth = width
            val parentWidth = parent.parent.let { it as View }.widthF
            val target = x - floatingWidth / 2f
            translationX = clamp(target, 0f, parentWidth - floatingWidth)
        }

        floatingText.text = Dates.tooltip(data.xs[idx])
        updateValues(idx)
    }

    private fun updateValues(idx: Idx) {
        val sum = enabledLines.sumByIndex { data.columns[it][idx] }
        lineTexts.forEach { id, view ->
            val value = data.columns[id][idx]
            val percent = (value * 100f) / sum
            view.percentage().text = "${percent.roundToInt()}%"
            view.value().text = tooltipValue(value)
        }
        all.text = tooltipValue(sum)
    }
}
