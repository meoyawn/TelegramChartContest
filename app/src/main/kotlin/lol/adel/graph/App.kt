package lol.adel.graph

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import lol.adel.graph.data.Chart
import lol.adel.graph.data.ChartAdapterFactory
import okio.Okio
import kotlin.system.measureTimeMillis

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
        val t = measureTimeMillis {
            val src = Okio.buffer(Okio.source(resources.openRawResource(R.raw.chart_data)))
            val type = Types.newParameterizedType(List::class.java, Chart::class.java)
            CHARTS = moshi.adapter<List<Chart>>(type).fromJson(src)!!
        }
        println("parsing took $t ms")
    }
}
