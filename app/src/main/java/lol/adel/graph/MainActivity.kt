package lol.adel.graph

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import help.isNight
import help.setNightMode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chart)

        val vh = ViewHolder(
            root = findViewById(R.id.parent),
            chartView = findViewById(R.id.chart),
            scroll = findViewById(R.id.scroll),
            background = findViewById(R.id.background),
            horizontalLabels = findViewById(R.id.horizontal_labels),
            name = findViewById(R.id.chart_name)
        )

        vh.setup(idx = 1)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.night, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.night -> {
                setNightMode(night = !resources.configuration.isNight())
                recreate()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
}
