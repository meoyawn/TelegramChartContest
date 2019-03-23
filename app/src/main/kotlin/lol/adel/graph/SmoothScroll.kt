package lol.adel.graph

import help.*

class SmoothScroll {

    var startDir = Direction.NONE
    var endDir = Direction.NONE

    val visible = MinMax(0f, 0f)
    val anticipated = MinMax(0f, 0f)

    var currentMax: Float = 0f
    var currentMaxIdx: IdxF = 0f
    var anticipatedMax: Long = 0L
    var anticipatedMaxIdx: Idx = 0

    fun cameraYMax(cameraX: MinMax): Float =
        smooth(
            visibleStart = visible.min,
            visibleEnd = visible.max,

            anticipatedStart = anticipated.min,
            anticipatedEnd = anticipated.max,

            currentMax = currentMax,
            currentMaxIdx = currentMaxIdx,
            anticipatedMax = anticipatedMax.toFloat(),
            anticipatedMaxIdx = anticipatedMaxIdx.toFloat(),

            start = cameraX.min,
            end = cameraX.max
        )
}

private fun smooth(
    visibleStart: Float,
    visibleEnd: Float,
    anticipatedStart: Float,
    anticipatedEnd: Float,

    currentMax: Float,
    currentMaxIdx: IdxF,
    anticipatedMax: Float,
    anticipatedMaxIdx: IdxF,

    start: Float,
    end: Float
): Float {
    val startDiff = anticipatedStart - visibleStart
    val endDiff = anticipatedEnd - visibleEnd
    return when {
        anticipatedMax > currentMax -> {
            when (startEnd(startDiff, endDiff, goingUp = true)) {
                StartEnd.START ->
                    if (start in anticipatedMaxIdx..visibleStart) {
                        val norm = normalize(start, anticipatedMaxIdx, visibleStart)
                        denormalize(value = 1 - norm, min = currentMax, max = anticipatedMax)
                    } else {
                        anticipatedMax
                    }

                StartEnd.END ->
                    if (end in visibleEnd..anticipatedMaxIdx) {
                        val norm = normalize(end, visibleEnd, anticipatedMaxIdx)
                        denormalize(norm, currentMax, anticipatedMax)
                    } else {
                        anticipatedMax
                    }
            }
        }

        anticipatedMax == currentMax ->
            currentMax

        else ->
            when (startEnd(startDiff, endDiff, goingUp = false)) {
                StartEnd.START ->
                    smooth(
                        visibleStart = anticipatedMaxIdx,
                        visibleEnd = anticipatedEnd,

                        currentMax = anticipatedMax,
                        currentMaxIdx = anticipatedMaxIdx,

                        anticipatedStart = visibleStart,
                        anticipatedEnd = visibleEnd,
                        anticipatedMax = currentMax,
                        anticipatedMaxIdx = currentMaxIdx,

                        start = start,
                        end = end
                    )

                StartEnd.END ->
                    smooth(
                        visibleStart = anticipatedStart,
                        visibleEnd = anticipatedMaxIdx,

                        currentMax = anticipatedMax,
                        currentMaxIdx = anticipatedMaxIdx,

                        anticipatedStart = visibleStart,
                        anticipatedEnd = visibleEnd,
                        anticipatedMax = currentMax,
                        anticipatedMaxIdx = currentMaxIdx,

                        start = start,
                        end = end
                    )
            }
    }
}
