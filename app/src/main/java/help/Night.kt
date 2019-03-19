package help

import android.content.res.Configuration

fun Configuration.isNight(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

fun setNightMode(night: Boolean) {
//    val mode = when {
//        night ->
//            AppCompatDelegate.MODE_NIGHT_YES
//
//        else ->
//            AppCompatDelegate.MODE_NIGHT_NO
//    }
//
//    AppCompatDelegate.setDefaultNightMode(mode)
}
