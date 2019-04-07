package lol.adel.graph

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import help.*
import lol.adel.graph.data.lineIds
import lol.adel.graph.data.xs

class MainActivity : Activity() {

    private companion object {
        const val ID_NIGHT = 100500
        const val ID_SCROLL = 100501
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
            dividerDrawable = ctx.getDrawable(R.drawable.charts_divider)
        }

        App.CHARTS.forEachIndexed { idx, data ->
            val lineIds = data.lineIds()
            val xs = data.xs()

            val vh = makeChartLayout(ctx = ctx, medium = Typefaces.medium, data = data, lineIds = lineIds, xs = xs)
            vh.setup(idx, data, lineIds, xs)

            root += vh.root
        }

        setContentView(ScrollView(ctx).apply {
            id = ID_SCROLL
            addView(root)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.night_mode, ID_NIGHT, 0, R.string.night_mode).apply {
            setIcon(R.drawable.ic_moon)
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
