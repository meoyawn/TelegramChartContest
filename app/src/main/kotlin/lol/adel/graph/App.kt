package lol.adel.graph

import android.app.Application
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import lol.adel.graph.data.Chart
import lol.adel.graph.data.ChartAdapterFactory
import okio.Okio

class App : Application() {

    companion object {
        lateinit var app: App
    }

    private val moshi = Moshi.Builder()
        .add(ChartAdapterFactory)
        .build()

    val charts by lazy {
        val src = Okio.buffer(Okio.source(resources.openRawResource(R.raw.chart_data)))
        val type = Types.newParameterizedType(List::class.java, Chart::class.java)
        moshi.adapter<List<Chart>>(type).fromJson(src)!!
    }

    override fun onCreate() {
        app = this
        super.onCreate()
    }
}
