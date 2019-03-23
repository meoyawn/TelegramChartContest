package lol.adel.graph

import android.app.Activity
import android.app.Fragment
import android.app.FragmentTransaction
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import help.*
import lol.adel.graph.data.CHARTS
import lol.adel.graph.data.chartName

class ListFragment : Fragment() {

    companion object {

        private val listId = View.generateViewId()

        fun toggleNight(act: Activity) =
            act.findViewById<ViewGroup?>(listId)?.forEach {
                when {
                    it is TextView -> {
                        it.animateTextColor(R.color.floating_text)
                        it.background = act.getDrawable(act.attr(android.R.attr.selectableItemBackground))
                    }

                    isDivider(it) ->
                        it.animateBackground(R.color.divider)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup, savedInstanceState: Bundle?): View {
        val ctx = activity

        activity.actionBar?.setDisplayHomeAsUpEnabled(false)

        return ScrollView(ctx).apply {
            isFillViewport = true

            addView(LinearLayout(ctx).apply {
                id = listId
                orientation = LinearLayout.VERTICAL

                for (idx in CHARTS.indices) {
                    addView(TextView(ctx).apply {
                        text = chartName(idx)
                        background = ctx.getDrawable(ctx.attr(android.R.attr.selectableItemBackground))
                        minHeight = 56.dp
                        gravity = Gravity.CENTER_VERTICAL
                        textSize = 18f
                        setTextColor(ctx.color(R.color.floating_text))
                        updatePadding(left = 16.dp, right = 16.dp)
                        setOnClickListener {
                            fragmentManager.sync {
                                beginTransaction()
                                    .replace(android.R.id.content, ChartFragment.newInstance(idx))
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    .addToBackStack(null)
                                    .commit()
                            }
                        }
                    })
                    addView(
                        View(ctx).apply { setBackgroundResource(R.color.divider) },
                        LinearLayout.LayoutParams(MATCH_PARENT, 1.dp)
                    )
                }

                if (childCount > 0) {
                    removeViewAt(childCount - 1)
                }

                addView(TextView(ctx).apply {
                    updatePadding(top = 16.dp)
                    textSize = 16f
                    gravity = Gravity.CENTER
                    text = "Multitouch supported on the scroll bar"
                    setTextColor(ctx.color(R.color.floating_text))
                })
            })
        }
    }
}
