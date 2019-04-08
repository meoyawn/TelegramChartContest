package lol.adel.graph.widget

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.CycleInterpolator
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.*
import lol.adel.graph.data.*

@SuppressLint("ViewConstructor")
class ChartParent(ctx: Context, val data: Chart, idx: Idx) : LinearLayout(ctx) {

    private companion object {
        val NAMES = listOf("Followers", "Interactions", "Growth", "Messages", "Views", "Apps")

        val ID = View.generateViewId()

        val TWO_TIMES = CycleInterpolator(2f)

        fun makeLineText(ctx: Context, chart: Chart, id: LineId, medium: Typeface): ViewGroup =
            LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL

                val color = chart.color(id)

                addView(TextView(ctx).apply {
                    textSize = 18f
                    setTextColor(color)
                    typeface = medium
                })

                addView(TextView(ctx).apply {
                    textSize = 16f
                    setTextColor(color)
                    text = chart.names[id]
                })
            }
    }

    val name: TextView
    val chartView: ChartView
    val floating: ViewGroup
    val floatingText: TextView
    val floatingContainer: ViewGroup
    val xLabels: XLabelsView
    val preview: PreviewView
    val scroll: ScrollBarView
    val dates: TextView

    // state
    private val enabledLines: ArrayList<LineId>
    private val cameraX: MinMax

    // post state
    private val allLines = data.lineIds()
    private val lineCheckboxes = SimpleArrayMap<LineId, CheckBox>()
    private val lineTexts = SimpleArrayMap<LineId, ViewGroup>()

    init {
        val dataSize = data.size()
        val lastIndex = dataSize - 1

        val xs = data.xs()

        enabledLines = ArrayList(allLines)
        cameraX = MinMax(min = dataSize * 0.75f, max = dataSize - 1f)

        id = ID + idx
        orientation = LinearLayout.VERTICAL

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

        val lineBuffer = FloatArray(size = dataSize.inc() * 4)

        val configuration = ctx.resources.configuration
        val height = if (configuration.screenHeightDp > configuration.screenWidthDp) 270.dp else 130.dp

        addView(FrameLayout(ctx).apply {
            layoutTransition = LayoutTransition()

            chartView = ChartView(ctx, data, allLines, lineBuffer, cameraX, enabledLines)
            addView(chartView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            floating = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.floating_bg)
                elevation = 2.dpF
                updatePadding(left = 16.dp, top = 8.dp, right = 16.dp, bottom = 8.dp)

                floatingText = TextView(ctx).apply {
                    typeface = Typefaces.medium
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
        }, LinearLayout.LayoutParams(MATCH_PARENT, height))

        addView(FrameLayout(ctx).apply {
            xLabels = XLabelsView(ctx, xs, cameraX)
            addView(xLabels)
        }, LinearLayout.LayoutParams(MATCH_PARENT, 36.dp))

        addView(FrameLayout(ctx).apply {

            addView(FrameLayout(ctx).apply {
                preview = PreviewView(ctx, data, allLines, lineBuffer, enabledLines)
                addView(preview, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

            scroll = ScrollBarView(ctx, dataSize)
            addView(scroll, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
        }, LinearLayout.LayoutParams(MATCH_PARENT, 48.dp).apply {
            bottomMargin = 10.dp
        })

        val root = this

        allLines.forEachByIndex { id ->
            val checkBox = makeCheckBox(id)
            root.addView(checkBox)
            lineCheckboxes[id] = checkBox

            root.addView(
                View(ctx).apply { setBackgroundResource(R.color.divider) },
                LinearLayout.LayoutParams(MATCH_PARENT, 1.dp).apply {
                    marginStart = 40.dp
                }
            )

            val text = makeLineText(ctx, data, id, Typefaces.medium)
            floatingContainer.addView(text)
            lineTexts[id] = text
        }
        root.removeViewAt(root.childCount - 1)

        scroll.listener = object : ScrollBarView.Listener {
            override fun onBoundsChange(left: Float, right: Float) {

                cameraX.set(left * lastIndex, right * lastIndex)
                chartView.cameraXChanged()
                xLabels.cameraXChanged()

                val range = Dates.HEADER_RANGE
                dates.text =
                    "${range.format(xs[(left * lastIndex).toInt()])} - ${range.format(xs[(right * lastIndex).toInt()])}"
            }
        }

        floating.visibility = View.INVISIBLE
        chartView.listener = object : ChartView.Listener {
            override fun onTouch(idx: Idx, x: PxF, maxY: Float) {
                floating.visibility = visibleOrInvisible(idx != -1)

                if (idx in 0..lastIndex) {
                    val floatingWidth = floating.width

                    val target = x - 20.dp
                    val altTarget = x - floatingWidth + 40.dp

                    val rightOk = target + floatingWidth <= chartView.width

                    floating.translationX = when {
                        target > 0 && rightOk ->
                            target

                        !rightOk && altTarget > 0 ->
                            altTarget

                        else ->
                            0f
                    }

                    floatingText.text = Dates.PANEL.format(xs[idx])

                    lineTexts.forEach { id, view ->
                        view.component1().toTextView().text = chartValue(data.columns[id][idx], maxY)
                    }
                }
            }
        }
    }

    private fun makeCheckBox(id: LineId): CheckBox =
        CheckBox(context).apply {
            text = data.names[id]
            buttonTintList = ColorStateList.valueOf(data.color(id))
            minHeight = 48.dp
            gravity = Gravity.CENTER_VERTICAL
            textSize = 18f
            isChecked = true
            setTextColor(context.color(R.color.floating_text))
            updatePadding(left = 10.dp)
        }

    private fun CheckBox.lineChecked(id: LineId) {
        if (!isChecked && enabledLines.size == 1) {
            isChecked = true
            translationX = 0f
            animate()
                .translationXBy(8.dpF)
                .setInterpolator(TWO_TIMES)
                .start()
        } else {
            if (isChecked) {
                enabledLines += id
            } else {
                enabledLines -= id
            }
            chartView.lineSelected(id, isChecked)
            preview.lineSelected(id, isChecked)
            lineTexts[id]?.visibility = visibleOrGone(isChecked)
        }
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (scroll.left >= 0) return

        val w = measuredWidth.toFloat()
        val lastIndex = data.size() - 1
        scroll.left = norm(cameraX.min, 0, lastIndex) * w
        scroll.right = norm(cameraX.max, 0, lastIndex) * w

        allLines.forEachByIndex { id ->
            val checkBox = lineCheckboxes[id]

            if (id !in enabledLines) {
                checkBox?.isChecked = false
                checkBox?.jumpDrawablesToCurrentState()
                lineTexts[id]?.visibility = View.GONE
            }

            checkBox?.setOnCheckedChangeListener { _, _ ->
                checkBox.lineChecked(id)
            }
        }
    }
}
