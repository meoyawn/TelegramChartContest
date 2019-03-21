package lol.adel.graph

import help.IdxF
import help.denormalize
import help.normalize
import kotlin.math.abs
import kotlin.math.round

enum class StartEnd {
    START,
    END,
}

fun rnd(f: Float): Float =
    round(f * 10_000) / 10_000

fun startEnd(startDiffRaw: Float, endDiffRaw: Float): StartEnd {
    val startDiff = rnd(startDiffRaw)
    val endDiff = rnd(endDiffRaw)

    val absStartDiff = abs(startDiff)
    val absEndDiff = abs(endDiff)

    return when {
        startDiff == endDiff && startDiff > 0 ->
            StartEnd.START

        startDiff == endDiff && startDiff <= 0 ->
            StartEnd.END

        absStartDiff > absEndDiff ->
            StartEnd.START

        else ->
            StartEnd.END
    }
}

fun smooth(
    visibleStart: Float,
    visibleEnd: Float,
    currentMax: Float,
    currentMaxIdx: IdxF,
    anticipatedStart: Float,
    anticipatedEnd: Float,
    anticipatedMax: Float,
    anticipatedMaxIdx: IdxF,
    s: Float,
    e: Float
): Float {
    val startDiff = anticipatedStart - visibleStart
    val endDiff = anticipatedEnd - visibleEnd
    return when {
        anticipatedMax > currentMax -> {
            when (startEnd(startDiff, endDiff)) {
                StartEnd.START -> {
                    println("to 1 ${currentMax} at ${currentMaxIdx} to ${anticipatedMax} at ${anticipatedMaxIdx}")
                    println("${s} in ${anticipatedMaxIdx..visibleStart}")

                    if (s in anticipatedMaxIdx..visibleStart) {
                        val norm = normalize(s, anticipatedMaxIdx, visibleStart)
                        denormalize(value = 1 - norm, min = currentMax, max = anticipatedMax)
                    } else {
                        anticipatedMax
                    }
                }

                StartEnd.END -> {
                    println("to 2 ${currentMax} (${currentMaxIdx}) to ${anticipatedMax} (${anticipatedMaxIdx})")

                    if (e in visibleEnd..anticipatedMaxIdx) {
                        val norm = normalize(e, visibleEnd, anticipatedMaxIdx)
                        denormalize(norm, currentMax, anticipatedMax)
                    } else {
                        anticipatedMax
                    }
                }
            }
        }

        anticipatedMax == currentMax ->
            currentMax

        else ->
            when (startEnd(startDiff, endDiff)) {
                StartEnd.START -> {
                    println("reversing start from ${currentMax} (${currentMaxIdx}) to ${anticipatedMax} (${anticipatedMaxIdx})")
                    smooth(
                        visibleStart = anticipatedMaxIdx,
                        visibleEnd = anticipatedEnd,

                        anticipatedStart = visibleStart,
                        anticipatedEnd = visibleEnd,

                        currentMax = anticipatedMax,
                        currentMaxIdx = anticipatedMaxIdx,
                        anticipatedMax = currentMax,
                        anticipatedMaxIdx = currentMaxIdx,

                        s = s,
                        e = e
                    )
                }

                StartEnd.END -> {
                    println("reversing end from ${currentMax} at ${currentMaxIdx} to ${anticipatedMax} at ${anticipatedMaxIdx} ($startDiff $endDiff)")
                    smooth(
                        visibleStart = anticipatedStart,
                        visibleEnd = anticipatedMaxIdx,

                        anticipatedStart = visibleStart,
                        anticipatedEnd = visibleEnd,

                        currentMax = anticipatedMax,
                        currentMaxIdx = anticipatedMaxIdx,
                        anticipatedMax = currentMax,
                        anticipatedMaxIdx = currentMaxIdx,

                        s = s,
                        e = e
                    )
                }
            }
    }
}
