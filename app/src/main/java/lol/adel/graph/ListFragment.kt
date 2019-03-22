package lol.adel.graph

import android.app.Fragment
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import help.attr
import help.dp
import help.updatePadding
import lol.adel.graph.data.CHARTS
import lol.adel.graph.data.chartName

class ListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup, savedInstanceState: Bundle?): View {
        val ctx = activity

        activity.actionBar?.setDisplayHomeAsUpEnabled(false)

        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL

            for (idx in CHARTS.indices) {
                addView(TextView(ctx).apply {
                    text = chartName(idx)
                    setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackground))
                    minHeight = 56.dp
                    gravity = Gravity.CENTER_VERTICAL
                    textSize = 18f
//                    setTextColor(ctx.color(R.color.floating_text))
                    updatePadding(left = 16.dp, right = 16.dp)
                    setOnClickListener {
                        fragmentManager.beginTransaction()
                            .replace(android.R.id.content, ChartFragment.newInstance(idx))
                            .addToBackStack(null)
                            .commit()
                        fragmentManager.executePendingTransactions()
                    }
                })
                addView(ImageView(ctx).apply { setImageResource(R.drawable.h_divider_full) })
            }

            removeViewAt(childCount - 1)

            addView(TextView(ctx).apply {
                updatePadding(top = 16.dp)
                textSize = 16f
                gravity = Gravity.CENTER
                text = "Multitouch available on the scroll bar"
            })
        }
    }
}
