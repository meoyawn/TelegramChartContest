package lol.adel.graph

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
    val handle: Handle
)
