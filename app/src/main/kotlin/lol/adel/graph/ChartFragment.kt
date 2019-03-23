package lol.adel.graph

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import help.*
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

        fun toggleNight(act: Activity) {
            val vh = (act.contentFragment() as? ChartFragment)?.vh ?: return

            vh.run {
                name.animateTextColor(R.color.colorAccent)
                chartView.toggleNight()
                horizontalLabels.toggleNight()
                scroll.toggleNight()
                bottom.animateBackground(R.color.bottom)
                linear.forEach {
                    when {
                        it is CheckBox ->
                            it.animateTextColor(R.color.floating_text)

                        isDivider(it) ->
                            it.animateBackground(R.color.divider)
                    }
                }
                floating.background = ctx.getDrawable(R.drawable.floating_bg)
                floatingText.setTextColor(ctx.color(R.color.floating_text))
            }
        }
    }

    private lateinit var vh: ViewHolder

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        activity.actionBar?.setDisplayHomeAsUpEnabled(true)

        val data = CHARTS[idx()]
        val lineIds = data.lineIds()
        val xs = data.xs()

        vh = makeChartLayout(ctx = activity, medium = Typefaces.medium, data = data, lineIds = lineIds, xs = xs)
        vh.setup(idx(), data, lineIds, xs)

        return vh.root
    }
}
