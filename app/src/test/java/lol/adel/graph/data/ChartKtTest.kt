package lol.adel.graph.data

import help.crossingY
import help.yCoordinate
import org.junit.Test
import kotlin.math.sqrt

class ChartKtTest {

    @Test
    fun crossing() {
        assertEquals(
            1f, crossingY(
                x1 = 1f,
                y1 = 4f,
                x2 = 4f,
                y2 = 1f,
                x3 = 4f,
                y3 = 0f,
                x4 = 4f,
                y4 = 1f
            )
        )
    }

    @Test
    fun topToBottom() {
        yCoordinate(
            radius = sqrt(8f),
            x = 5f,
            x1 = 1f,
            y1 = 5f,
            x2 = 5f,
            y2 = 1f
        ) { below, above ->
            assertEquals(5f, above)
        }
    }

    @Test
    fun bottomToTop() {
        yCoordinate(
            radius = sqrt(8f),
            x = 5f,
            x1 = 1f,
            y1 = 1f,
            x2 = 5f,
            y2 = 5f
        ) { below, above ->
            assertEquals(1f, below)
            assertEquals(5f + sqrt(8f), above)
        }
    }
}
