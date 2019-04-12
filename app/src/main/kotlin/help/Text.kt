package help

import kotlin.math.min

inline fun diff(from: String, to: String, f: (old: String, new: String, unchanged: String) -> Unit) {
    val lastFrom = from.lastIndex
    val lastTo = to.lastIndex

    for (i in 0..min(lastFrom, lastTo)) {
        if (from[lastFrom - i] != to[lastTo - i]) {
            f(from.dropLast(i), to.dropLast(i), to.takeLast(i))
            return
        }
    }

    f(from, to, "")
}
