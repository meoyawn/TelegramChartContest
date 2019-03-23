package lol.adel.graph

enum class Direction {
    FORWARD,
    BACKWARD,
    NONE;

    companion object {

        fun of(speed: Float): Direction =
            when {
                speed < 0 ->
                    BACKWARD

                speed > 0 ->
                    FORWARD

                else ->
                    NONE
            }
    }
}
