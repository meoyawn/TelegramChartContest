package lol.adel.graph.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.CycleInterpolator
import android.widget.Checkable
import androidx.collection.SimpleArrayMap
import help.*
import lol.adel.graph.data.Chart
import lol.adel.graph.data.LineId
import lol.adel.graph.data.color
import lol.adel.graph.widget.RoundCheckBox
import org.apmem.tools.layouts.FlowLayout

@SuppressLint("ViewConstructor")
class FilterView(ctx: Context, private val data: Chart, private val enabledLines: List<LineId>) : FlowLayout(ctx) {

    private companion object {
        val TWO_TIMES = CycleInterpolator(2f)

        // allocations
        val L1 = ArrayList<LineId>()
        val L2 = ArrayList<LineId>()

        fun recycle1(id: LineId): List<LineId> =
            L1.apply {
                clear()
                add(id)
            }

        fun recycle2(list: List<LineId>, without: LineId? = null): List<LineId> =
            L2.apply {
                clear()
                addAll(list)
                without?.let { remove(it) }
            }
    }

    interface Listener {
        fun onChange(select: List<LineId>, deselect: List<LineId>)
    }

    private val checkboxes = SimpleArrayMap<LineId, Checkable>()

    var listener: Listener? = null

    init {
        updatePadding(left = 8.dp, right = 8.dp)
        if (data.lineIds.size > 1) {
            data.lineIds.forEachByIndex { id ->
                val checkBox = makeCheckBox(lineId = id, initialCheck = id in enabledLines)
                checkboxes[id] = checkBox
                addView(checkBox as View)
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

    private fun Checkable.shake() {
        this as View
        isChecked = true
        translationX = 0f
        animate()
            .translationXBy(8.dpF)
            .setInterpolator(TWO_TIMES)
            .start()
    }

    private fun makeCheckBox(lineId: LineId, initialCheck: Boolean): Checkable =
        RoundCheckBox(context).apply {
            text = data.names[lineId]!!
            color = data.color(lineId)
            isChecked = initialCheck

            layoutParams = FlowLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply { marginStart = 8.dp }

            setOnClickListener {
                val idAsList = recycle1(lineId)
                if (enabledLines == idAsList) {
                    shake()
                } else {
                    if (lineId in enabledLines) {
                        onChange(deselect = idAsList)
                    } else {
                        onChange(select = idAsList)
                    }
                }
            }

            setOnLongClickListener {
                val idAsList = recycle1(lineId)
                if (enabledLines == idAsList) {
                    shake()
                } else {
                    if (lineId in enabledLines) {
                        onChange(deselect = recycle2(list = enabledLines, without = lineId))
                    } else {
                        onChange(select = idAsList, deselect = recycle2(enabledLines))
                    }
                }
                true
            }
        }
}
