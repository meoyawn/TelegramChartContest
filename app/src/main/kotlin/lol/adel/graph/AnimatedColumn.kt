package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Paint
import android.graphics.Path
import help.Idx
import help.Norm

class AnimatedColumn(
    val points: LongArray,
    val animator: ValueAnimator,
    val paint: Paint,
    val path: Path,
    var frac: Norm = 1f
)

operator fun AnimatedColumn.get(idx: Idx): Long =
    (points[idx] * frac).toLong()
