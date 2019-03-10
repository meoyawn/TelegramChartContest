package help

inline fun <T, R : Any> memoize(crossinline f: (T) -> R): (T) -> R {
    val map = simpleArrayMapOf<T, R>()
    return { key ->
        map[key] ?: f(key).also { map[key] = it }
    }
}
