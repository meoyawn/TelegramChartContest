package lol.adel.graph

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import help.ctx
import help.isNight
import help.setNightMode
import lol.adel.graph.widget.ChartParent

class MainActivity : Activity() {

    private companion object {
        const val ID_NIGHT = 100500
        const val ID_SCROLL = 100501
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val charts = App.CHARTS
        val fattest = charts.maxBy { it.size * it.lineIds.size }!!
        val lineBuffer = FloatArray(size = fattest.size.inc() * fattest.lineIds.size * 4)

        setContentView(ScrollView(ctx).apply {
            id = ID_SCROLL

            addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerDrawable = ctx.getDrawable(R.drawable.charts_divider)

                charts.forEachIndexed { idx, data ->
                    addView(ChartParent(ctx, data, idx, lineBuffer))
                }
            })
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.night_mode, ID_NIGHT, 0, R.string.night_mode).apply {
            setIcon(R.drawable.ic_night)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            ID_NIGHT -> {
                setNightMode(night = !resources.configuration.isNight())
                recreate()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
