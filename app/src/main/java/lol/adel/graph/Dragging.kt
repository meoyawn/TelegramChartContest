package lol.adel.graph

import help.X

sealed class Dragging {

    object Left : Dragging()

    object Right : Dragging()

    data class Between(
        val left: X,
        val right: X,
        val x: X
    ) : Dragging()
}
