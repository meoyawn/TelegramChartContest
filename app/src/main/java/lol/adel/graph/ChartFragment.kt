package lol.adel.graph

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import help.Idx

class ChartFragment : Fragment() {

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

        activity.actionBar?.setDisplayHomeAsUpEnabled(true)

        val vh = view.run {
            ViewHolder(
                linear = findViewById(R.id.parent),
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
