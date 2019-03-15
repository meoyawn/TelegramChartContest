package help

inline fun <T, R : Any> memoize(crossinline f: (T) -> R): (T) -> R {
    val map = simpleArrayMapOf<T, R>()
    return { key ->
        map[key] ?: f(key).also { map[key] = it }
    }
}

inline fun <A, B, C> zip(a: A?, b: B?, f: (A, B) -> C): C? =
    a?.let {
        b?.let {
            f(a, b)
        }
    }
