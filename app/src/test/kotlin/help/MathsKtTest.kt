package help

import org.junit.Assert.assertEquals
import org.junit.Test

class MathsKtTest {

    @Test
    fun fwd() {
        assertEquals(0f, normalize(10f, 10f, 100f))
        assertEquals(0f, normalize(100f, 100f, 10f))

        assertEquals(1f, normalize(100f, 10f, 100f))
        assertEquals(1f, normalize(10f, 100f, 10f))

        assertEquals(0.5f, normalize(55f, 10f, 100f))
        assertEquals(0.5f, normalize(55f, 100f, 10f))
    }

    @Test
    fun back() {
        assertEquals(10f, denormalize(0f, 10f, 100f))
        assertEquals(100f, denormalize(0f, 100f, 10f))

        assertEquals(100f, denormalize(1f, 10f, 100f))
        assertEquals(10f, denormalize(1f, 100f, 10f))

        assertEquals(55f, denormalize(0.5f, 10f, 100f))
        assertEquals(55f, denormalize(0.5f, 100f, 10f))
    }
}
