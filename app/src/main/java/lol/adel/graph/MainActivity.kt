package lol.adel.graph

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import help.*
import lol.adel.graph.data.chartName

class ListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup, savedInstanceState: Bundle?): View {
        val ctx = container.context
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            for (idx in CHARTS.indices) {
                addView(TextView(ctx).apply {
                    text = chartName(idx)
                    setBackgroundResource(ctx.attr(android.R.attr.selectableItemBackground))
                    minHeight = 48.dp
                    gravity = Gravity.CENTER_VERTICAL
                    textSize = 18f
                    setTextColor(ctx.color(R.color.floating_text))
                    updatePadding(left = 16.dp, right = 16.dp)
                    setOnClickListener {
                        fragmentManager.beginTransaction()
                            .replace(android.R.id.content, ChartFragment.newInstance(idx))
                            .addToBackStack(null)
                            .commit()
                    }
                })
                addView(ImageView(ctx).apply { setImageResource(R.drawable.h_divider_full) })
            }
            removeViewAt(childCount - 1)
        }
    }
}

class ChartFragment() : Fragment() {

    companion object {

        fun newInstance(idx: Idx): ChartFragment =
            ChartFragment().apply {
                arguments = Bundle().apply {
                    putInt("idx", idx)
                }
            }

        fun ChartFragment.idx(): Int =
            arguments.getInt("idx")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.chart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vh = view.run {
            ViewHolder(
                root = findViewById(R.id.parent),
                chartView = findViewById(R.id.chart),
                scroll = findViewById(R.id.scroll),
                background = findViewById(R.id.background),
                horizontalLabels = findViewById(R.id.horizontal_labels),
                name = findViewById(R.id.chart_name),
                floating = findViewById(R.id.floating_panel),
                floatingText = findViewById(R.id.floating_text),
                floatingContainer = findViewById(R.id.floating_container)
            )
        }

        vh.setup(idx())
    }
}

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(android.R.id.content, ListFragment())
                .commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.night, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.night -> {
                setNightMode(night = !resources.configuration.isNight())
                recreate()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
