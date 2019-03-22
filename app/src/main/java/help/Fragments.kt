package help

import android.app.Activity

@Suppress("DEPRECATION")
inline fun android.app.FragmentManager.sync(f: android.app.FragmentManager.() -> Unit) {
    f()
    executePendingTransactions()
}

@Suppress("DEPRECATION")
fun Activity.contentFragment(): android.app.Fragment? =
    fragmentManager.findFragmentById(android.R.id.content)
