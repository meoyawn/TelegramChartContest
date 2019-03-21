package lol.adel.graph

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import help.isNight
import help.setNightMode

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                .replace(android.R.id.content, ListFragment())
                .commit()
        }
        fragmentManager.executePendingTransactions()
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
                setNightMode(night = !resources.configuration.isNight())
                recreate()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
