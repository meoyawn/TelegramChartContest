package help

import android.content.res.Configuration
import android.content.res.Resources

fun Configuration.isNight(): Boolean =
    (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

fun Resources.setNightMode(night: Boolean) {
    val newNightMode = when {
        night ->
            Configuration.UI_MODE_NIGHT_YES

        else ->
            Configuration.UI_MODE_NIGHT_NO
    }

    val config = Configuration(configuration).apply {
        uiMode = newNightMode or (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv())
    }
    updateConfiguration(config, displayMetrics)
}
