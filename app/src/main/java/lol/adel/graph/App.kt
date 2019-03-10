package lol.adel.graph

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import help.ctx

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(ctx)
    }
}
