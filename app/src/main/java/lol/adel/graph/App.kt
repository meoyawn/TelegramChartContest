package lol.adel.graph

import android.app.Application
import help.setNightMode

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        setNightMode(night = false)
    }
}
