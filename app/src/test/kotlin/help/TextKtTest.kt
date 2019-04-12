package help

import org.junit.Assert.assertEquals
import org.junit.Test

class TextKtTest {

    @Test
    fun ddd() {
        firstChangeFromEnd(from = "Wed, 7 Nov 2018", to = "Fri, 15 Jun 2018") { old, new, unchanged ->
            assertEquals("Wed, 7 Nov", old)
            assertEquals("Fri, 15 Jun", new)
            assertEquals(" 2018", unchanged)
        }
        firstChangeFromEnd(from = "huy", to = "pizda") { old, new, unchanged ->
            assertEquals("huy", old)
            assertEquals("pizda", new)
            assertEquals("", unchanged)
        }
        firstChangeFromEnd(from = "", to = "new") { old, new, unchanged ->
            assertEquals("", old)
            assertEquals("new", new)
            assertEquals("", unchanged)
        }
    }
}
