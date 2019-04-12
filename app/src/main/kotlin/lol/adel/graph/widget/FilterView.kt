package lol.adel.graph.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.animation.CycleInterpolator
import android.widget.CheckBox
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.R
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId
import lol.adel.graph.data.color
import org.apmem.tools.layouts.FlowLayout

@SuppressLint("ViewConstructor")
class FilterView(ctx: Context, private val data: Chart, private val enabledLines: List<LineId>) : FlowLayout(ctx) {

    private companion object {
        val TWO_TIMES = CycleInterpolator(2f)
    }

    interface Listener {
        fun onChange(select: List<LineId>, deselect: List<LineId>)
    }

    private val checkboxes = SimpleArrayMap<LineId, CheckBox>()

    var listener: Listener? = null

    init {
        if (data.lineIds.size > 1) {
            data.lineIds.forEachByIndex { id ->
                val checkBox = makeCheckBox(lineId = id, initialCheck = id in enabledLines)
                checkboxes[id] = checkBox
                addView(checkBox)
            }
        }
    }

    private fun onChange(select: List<LineId> = emptyList(), deselect: List<LineId> = emptyList()) {
        deselect.forEachByIndex {
            checkboxes[it]?.isChecked = false
        }
        select.forEachByIndex {
            checkboxes[it]?.isChecked = true
        }
        listener?.onChange(select, deselect)
    }

    private fun CheckBox.shake() {
        isChecked = true
        translationX = 0f
        animate()
            .translationXBy(8.dpF)
            .setInterpolator(TWO_TIMES)
            .start()
    }

    private fun makeCheckBox(lineId: LineId, initialCheck: Boolean): CheckBox =
        CheckBox(context).apply {
            text = data.names[lineId]
            buttonTintList = ColorStateList.valueOf(data.color(lineId))
            minHeight = 48.dp
            gravity = Gravity.CENTER_VERTICAL
            textSize = 18f
            isChecked = initialCheck
            setTextColor(context.color(R.attr.floating_text))

            layoutParams = FlowLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { marginStart = 8.dp }
            updatePadding(left = 10.dp)

            setOnClickListener {
                val idAsList = listOf(lineId)
                if (enabledLines == idAsList) {
                    shake()
                } else {
                    if (lineId in enabledLines) {
                        onChange(deselect = listOf(lineId))
                    } else {
                        onChange(select = listOf(lineId))
                    }
                }
            }

            setOnLongClickListener {
                val idAsList = listOf(lineId)
                if (enabledLines == idAsList) {
                    shake()
                } else {
                    if (lineId in enabledLines) {
                        onChange(deselect = enabledLines - lineId)
                    } else {
                        onChange(select = idAsList, deselect = enabledLines.toList())
                    }
                }
                true
            }
        }
}
