package help

import android.content.Context
import android.content.res.Configuration

fun Configuration.isNight(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

@Suppress("DEPRECATION")
fun Context.setNightMode(night: Boolean) {
    val newNightMode = when {
        night ->
            Configuration.UI_MODE_NIGHT_YES

        else ->
            Configuration.UI_MODE_NIGHT_NO
    }

    val res = resources
    val configuration = res.configuration
    val config = Configuration(configuration).apply {
        uiMode = newNightMode or (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
    }
    res.updateConfiguration(config, res.displayMetrics)
}
