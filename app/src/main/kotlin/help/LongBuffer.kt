package help

class LongBuffer {

    private var buf = LongArray(size = 12)
    private var size: Int = 0

    operator fun plusAssign(l: Long) {
        if (buf.size == size) {
            val new = LongArray(size = buf.size * 2)
            buf.copyInto(new)
            buf = new
        }
        buf[size] = l
        size++
    }

    fun reset() {
        size = 0
    }

    fun toArray(): LongArray =
        LongArray(size).also {
            buf.copyInto(it, endIndex = size)
        }
}
