package help

inline fun <T> List<T>.forEachByIndex(f: (T) -> Unit) {
    for (i in indices) {
        f(get(i))
    }
}

inline fun <T> List<T>.sumByIndex(f: (T) -> Long): Long {
    var sum = 0L
    forEachByIndex {
        sum += f(it)
    }
    return sum
}
