package lol.adel.graph

import org.junit.Assert.assertEquals
import org.junit.Test

class SmoothScrollKtTest {

    @Test
    fun startEnd() {
        assertEquals(StartEnd.START, startEnd(0.3418f, 0.3418f))
        assertEquals(StartEnd.END, startEnd(-0.3418f, -0.3418f))
        assertEquals(StartEnd.END, startEnd(0f, -0.3418f))
        assertEquals(StartEnd.START, startEnd(-0.3418f, 0f))
        assertEquals(StartEnd.END, startEnd(-0.3418f, 0.3419f))
    }
}
