package lol.adel.graph

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import help.MATCH_PARENT
import help.WRAP_CONTENT
import help.ctx
import lol.adel.graph.widget.ChartParent

class MainActivity : Activity() {

    private companion object {
        const val ID_NIGHT = 100500
        const val ID_SCROLL = 100501

        var night = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val charts = App.CHARTS
        val fattest = charts.maxBy { it.size * it.lineIds.size }!!
        val lineBuffer = FloatArray(size = fattest.size.inc() * fattest.lineIds.size * 4)

        setTheme(if (night) R.style.AppTheme_Dark else R.style.AppTheme_Light)
        setContentView(ScrollView(ctx).apply {
            id = ID_SCROLL

            addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
                dividerDrawable = ctx.getDrawable(R.drawable.charts_divider)

                for (idx in charts.indices) {
                    addView(
                        ChartParent(ctx, charts[idx], idx, lineBuffer),
                        LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                    )
                }
            }, FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        }, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
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
                night = !night
                recreate()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
