package lol.adel.graph

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import help.*
import lol.adel.graph.data.CHARTS
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
        }

        for (idx in CHARTS.indices) {
            val data = CHARTS[idx]
            val lineIds = data.lineIds()
            val xs = data.xs()

            val vh = makeChartLayout(ctx = ctx, medium = Typefaces.medium, data = data, lineIds = lineIds, xs = xs)
            vh.setup(idx, data, lineIds, xs)

            root.addView(vh.root)
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
            android.R.id.home -> {
                fragmentManager.popBackStackImmediate()
                true
            }

            ID_NIGHT -> {
                val oldBg = color(R.color.background)
                val oldToolbar = color(R.color.colorPrimary)
                setNightMode(night = !resources.configuration.isNight())
                setTheme(R.style.AppTheme)

                animateColor(window.statusBarColor, color(R.color.colorPrimaryDark)) {
                    window.statusBarColor = it
                }.start()

                val toolbarBg = ColorDrawable(oldToolbar)
                actionBar?.setBackgroundDrawable(toolbarBg)
                toolbarBg.animate(color(R.color.colorPrimary))

                val windowBg = ColorDrawable(oldBg)
                window.setBackgroundDrawable(windowBg)
                windowBg.animate(color(R.color.background))

                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
