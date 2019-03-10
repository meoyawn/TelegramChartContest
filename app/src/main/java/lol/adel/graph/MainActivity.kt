package lol.adel.graph

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val vh = ViewHolder(
            root = findViewById(R.id.parent),
            chartView = findViewById(R.id.chart),
            scroll = findViewById(R.id.scroll)
        )

        vh.setup(CHARTS.first())
    }
}
