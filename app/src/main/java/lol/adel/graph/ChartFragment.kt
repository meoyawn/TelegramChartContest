package lol.adel.graph

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import help.Idx
import lol.adel.graph.data.CHARTS
import lol.adel.graph.data.lineIds
import lol.adel.graph.data.xs

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val data = CHARTS[idx()]
        val lineIds = data.lineIds()
        val xs = data.xs()
        val vh = makeChartLayout(ctx = activity, medium = Typefaces.medium, data = data, lineIds = lineIds, xs = xs)
        vh.setup(idx(), data, lineIds, xs)
        return vh.root
    }
}
