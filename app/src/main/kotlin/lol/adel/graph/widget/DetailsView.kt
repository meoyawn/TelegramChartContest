package lol.adel.graph.widget

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.Dates
import lol.adel.graph.R
import lol.adel.graph.Typefaces
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId
import lol.adel.graph.data.color
import lol.adel.graph.data.get

@SuppressLint("ViewConstructor")
class DetailsView(ctx: Context, val data: Chart, val enabledLines: List<LineId>) : LinearLayout(ctx) {

    private companion object {
        fun makeLineText(ctx: Context, chart: Chart, id: LineId, medium: Typeface): ViewGroup =
            LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL

                addView(TextView(ctx).apply {
                    textSize = 16f
                    text = chart.names[id]
                })

                addView(TextView(ctx).apply {
                    textSize = 18f
                    setTextColor(chart.color(id))
                    typeface = medium
                })
            }
    }

    private val floatingText: TextView
    private val floatingContainer: ViewGroup

    private val lineTexts = SimpleArrayMap<LineId, ViewGroup>()

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.floating_bg)
        elevation = 2.dpF
        updatePadding(left = 16.dp, top = 8.dp, right = 16.dp, bottom = 8.dp)

        floatingText = TextView(ctx).apply {
            typeface = Typefaces.medium
            setTextColor(ctx.color(R.attr.floating_text))
            textSize = 17f
        }
        addView(floatingText)

        floatingContainer = LinearLayout(ctx).apply {
            layoutTransition = LayoutTransition()
            orientation = LinearLayout.VERTICAL
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerDrawable = ctx.getDrawable(R.drawable.h_space_16)
        }
        addView(floatingContainer)

        data.lineIds.forEachByIndex { id ->
            val text = makeLineText(ctx, data, id, Typefaces.medium)
            floatingContainer.addView(text)
            lineTexts[id] = text
        }
    }

    fun lineChecked(select: List<LineId>, deselect: List<LineId>) {
        deselect.forEachByIndex {
            lineTexts[it]?.visibility = View.GONE
        }
        select.forEachByIndex {
            lineTexts[it]?.visibility = View.VISIBLE
        }
    }

    fun show(idx: Idx, x: PxF) {
        val floatingWidth = width
        val parentWidth = parent.parent.let { it as View }.width

        val target = x - 20.dp
        val altTarget = x - floatingWidth + 40.dp
        val rightOk = target + floatingWidth <= parentWidth

        translationX = when {
            target > 0 && rightOk ->
                target

            !rightOk && altTarget > 0 ->
                altTarget

            else ->
                0f
        }

        floatingText.text = Dates.PANEL.format(data.xs[idx])
        lineTexts.forEach { id, view ->
            view.component2().toTextView().text = data.columns[id][idx].toString()
        }
    }
}
