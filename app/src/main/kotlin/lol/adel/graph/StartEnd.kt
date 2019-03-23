package lol.adel.graph

import kotlin.math.abs
import kotlin.math.round

enum class StartEnd {
    START,
    END,
}

private fun rnd(f: Float): Float =
    round(f * 10_000) / 10_000

fun startEnd(startDiffRaw: Float, endDiffRaw: Float, goingUp: Boolean): StartEnd {
    val startDiff = rnd(startDiffRaw)
    val endDiff = rnd(endDiffRaw)

    val absStartDiff = abs(startDiff)
    val absEndDiff = abs(endDiff)

    val forward = startDiff > 0
    val backward = !forward

    return when {
        startDiff == endDiff && forward ->
            if (goingUp) StartEnd.END
            else StartEnd.START

        startDiff == endDiff && backward ->
            if (goingUp) StartEnd.START
            else StartEnd.END

        absStartDiff > absEndDiff ->
            StartEnd.START

        else ->
            StartEnd.END
    }
}
