package help

import org.junit.Assert.assertEquals
import org.junit.Test

class MathsKtTest {

    @Test
    fun fwd() {
        assertEquals(0f, norm(10f, 10f, 100f))
        assertEquals(0f, norm(100f, 100f, 10f))

        assertEquals(1f, norm(100f, 10f, 100f))
        assertEquals(1f, norm(10f, 100f, 10f))

        assertEquals(0.5f, norm(55f, 10f, 100f))
        assertEquals(0.5f, norm(55f, 100f, 10f))
    }

    @Test
    fun back() {
        assertEquals(10f, denorm(0f, 10f, 100f))
        assertEquals(100f, denorm(0f, 100f, 10f))

        assertEquals(100f, denorm(1f, 10f, 100f))
        assertEquals(10f, denorm(1f, 100f, 10f))

        assertEquals(55f, denorm(0.5f, 10f, 100f))
        assertEquals(55f, denorm(0.5f, 100f, 10f))
    }
}
