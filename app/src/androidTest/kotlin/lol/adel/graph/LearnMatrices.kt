package lol.adel.graph

import android.graphics.Matrix
import org.junit.Assert.assertEquals
import org.junit.Test

class LearnMatrices {

    @Test
    fun lol() {
        val cameraX = MinMax(min = 2f, max = 18f)
        val cameraY = MinMax(min = 5000f, max = 10000f)

        val width = 1080f
        val height = 1920f

        val m = Matrix().apply {
            setup(cameraX = cameraX, cameraY = cameraY, right = width, bottom = height, top = 0f)
        }
        println(m)

        val result = FloatArray(2)
        m.mapPoints(result, floatArrayOf(10f, 7500f))

        assertEquals(width / 2, result.first())
        assertEquals(height / 2, result.last())
    }
}
