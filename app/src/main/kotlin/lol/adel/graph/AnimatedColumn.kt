package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Paint
import android.graphics.Path
import help.Idx
import help.Norm

class AnimatedColumn(
    val points: LongArray,
    var frac: Norm,
    val animator: ValueAnimator,
    val paint: Paint,
    val path: Path
)

operator fun AnimatedColumn.get(idx: Idx): Long =
    (points[idx] * frac).toLong()
