package help

fun <T, R> memoize(f: (T) -> R): (T) -> R {
    val map = simpleArrayMapOf<T, R>()
    return { t -> map[t] ?: f(t).also { map[t] = it } }
}
