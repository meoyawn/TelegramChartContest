package lol.adel.graph

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import help.*
import lol.adel.graph.data.Chart1
import lol.adel.graph.data.Chart1AdapterFactory
import okio.Okio
import kotlin.system.measureTimeMillis

class MainActivity : Activity() {

    private companion object {
        const val ID = 100500

        val moshi = Moshi.Builder()
            .add(Chart1AdapterFactory)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragmentManager.sync {
                beginTransaction()
                    .replace(android.R.id.content, ListFragment())
                    .commit()
            }
        }

        println(measureTimeMillis {
            val tpe = Types.newParameterizedType(List::class.java, Chart1::class.java)
            val src = Okio.buffer(Okio.source(resources.openRawResource(R.raw.chart_data)))
            moshi.adapter<List<Chart1>>(tpe).fromJson(src)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.night_mode, ID, 0, R.string.night_mode).apply {
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

            ID -> {
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

                ListFragment.toggleNight(act)
                ChartFragment.toggleNight(act)
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
