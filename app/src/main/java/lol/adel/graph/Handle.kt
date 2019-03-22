package lol.adel.graph

import android.animation.Animator
import help.X

sealed class Handle {

    object Left : Handle()

    object Right : Handle()

    data class Between(
        val left: X,
        val right: X,
        val x: X
    ) : Handle()
}

data class Dragging(
    var radius: Float,
    val handle: Handle,
    var anim: Animator
)
