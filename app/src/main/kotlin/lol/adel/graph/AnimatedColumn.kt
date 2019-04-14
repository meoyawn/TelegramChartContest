package lol.adel.graph

import android.animation.ValueAnimator
import android.graphics.Paint
import help.Idx
import help.Norm

class AnimatedColumn(
    val points: LongArray,
    val animator: ValueAnimator,
    val paint: Paint,
    var frac: Norm = 1f
)

operator fun AnimatedColumn.get(idx: Idx): Float =
    points[idx] * frac
