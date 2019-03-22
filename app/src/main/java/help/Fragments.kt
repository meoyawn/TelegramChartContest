package help

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager

@Suppress("DEPRECATION")
inline fun FragmentManager.sync(f: FragmentManager.() -> Unit) {
    f()
    executePendingTransactions()
}

@Suppress("DEPRECATION")
fun Activity.contentFragment(): Fragment? =
    fragmentManager.findFragmentById(android.R.id.content)
