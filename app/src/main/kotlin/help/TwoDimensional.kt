package help

fun getPointIndex(i: Int, j: Int, jSize: Int): Int =
    (i * jSize + j) * 2

fun FloatArray.setPoint(i: Idx, j: Idx, jSize: Int, x: X, y: Y) {
    val idx = getPointIndex(i = i, j = j, jSize = jSize)
    this[idx + 0] = x
    this[idx + 1] = y
}

inline fun FloatArray.getPoint(i: Idx, j: Idx, jSize: Int, f: (x: X, y: Y) -> Unit) {
    val idx = getPointIndex(i = i, j = j, jSize = jSize)
    f(
        this[idx + 0],
        this[idx + 1]
    )
}
