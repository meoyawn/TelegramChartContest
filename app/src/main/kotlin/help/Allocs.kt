package help

inline fun <T> List<T>.forEachByIndex(f: (T) -> Unit) {
    for (i in indices) {
        f(get(i))
    }
}
