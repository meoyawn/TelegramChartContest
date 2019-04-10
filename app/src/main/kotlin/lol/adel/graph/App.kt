package lol.adel.graph

import android.app.Application
import android.content.res.AssetManager
import com.squareup.moshi.Moshi
import lol.adel.graph.data.Chart
import lol.adel.graph.data.ChartAdapterFactory
import okio.BufferedSource
import okio.Okio

class App : Application() {

    companion object {

        lateinit var CHARTS: List<Chart>
            private set

        fun AssetManager.openOkio(fileName: String): BufferedSource =
            Okio.buffer(Okio.source(open(fileName)))
    }

    override fun onCreate() {
        super.onCreate()

        val adapter = Moshi.Builder()
            .add(ChartAdapterFactory)
            .build()
            .adapter(Chart::class.java)

        CHARTS = assets.list("")!!.mapNotNull { dir ->
            dir.toIntOrNull()?.let {
                adapter.fromJson(assets.openOkio(fileName = "$dir/overview.json"))
            }
        }
    }
}
