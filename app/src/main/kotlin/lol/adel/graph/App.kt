package lol.adel.graph

import android.app.Application
import com.squareup.moshi.Moshi
import lol.adel.graph.data.Chart
import lol.adel.graph.data.ChartAdapterFactory
import okio.Okio

class App : Application() {

    companion object {

        lateinit var CHARTS: List<Chart>
            private set
    }

    private val moshi = Moshi.Builder()
        .add(ChartAdapterFactory)
        .build()

    override fun onCreate() {
        super.onCreate()

        CHARTS = assets.list("")!!.filter { it.toIntOrNull() != null }.map { folder ->
            val src = Okio.buffer(Okio.source(assets.open("$folder/overview.json")))
            moshi.adapter(Chart::class.java).fromJson(src)!!.apply {
                println(this)
            }
        }
    }
}
