package lol.adel.graph

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import help.ctx
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(ctx)
        Timber.plant(Timber.DebugTree())
    }
}
