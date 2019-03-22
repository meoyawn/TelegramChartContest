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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View =
        makeChartLayout(container.context, Typefaces.medium).run {
            activity.actionBar?.setDisplayHomeAsUpEnabled(true)

            setup(idx())

            root
        }
}
