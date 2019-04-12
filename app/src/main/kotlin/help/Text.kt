package help

import kotlin.math.min

fun diff(from: String, to: String): Idx {
    val lastFrom = from.lastIndex
    val lastTo = to.lastIndex

    for (i in 0..min(lastFrom, lastTo)) {
        if (from[lastFrom - i] != to[lastTo - i]) {
            return i
        }
    }

    return 0
}
