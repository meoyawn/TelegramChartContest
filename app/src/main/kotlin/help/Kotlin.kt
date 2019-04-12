package help

import androidx.collection.SimpleArrayMap

inline fun <T, R : Any> memoize(crossinline f: (T) -> R): (T) -> R {
    val map = SimpleArrayMap<T, R>()
    return { key ->
        map[key] ?: f(key).also { map[key] = it }
    }
}
