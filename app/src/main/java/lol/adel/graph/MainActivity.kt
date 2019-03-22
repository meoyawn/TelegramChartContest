package lol.adel.graph

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import help.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragmentManager.sync {
                beginTransaction()
                    .replace(android.R.id.content, ListFragment())
                    .commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.night, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                fragmentManager.popBackStackImmediate()
                true
            }

            R.id.night -> {
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
