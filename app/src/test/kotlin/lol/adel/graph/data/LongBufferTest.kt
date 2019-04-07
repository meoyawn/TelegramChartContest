package lol.adel.graph.data

import org.junit.Test
import java.util.*

class LongBufferTest {

    @Test
    fun toArray() {
        LongBuffer().run {
            plusAssign(1)
            plusAssign(2)
            plusAssign(3)

            check(Arrays.equals(toArray(), longArrayOf(1, 2, 3)))

            reset()

            check(Arrays.equals(toArray(), longArrayOf()))

            plusAssign(3)

            check(Arrays.equals(toArray(), longArrayOf(3)))
        }
    }
}
