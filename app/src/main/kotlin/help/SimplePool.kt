package help

class SimplePool<T>(maxPoolSize: Int) {

    private val pool: Array<Any?> = arrayOfNulls(maxPoolSize)

    var size: Int = 0
        private set

    fun acquire(): T? =
        if (size > 0) {
            val lastPooledIndex = size - 1

            @Suppress("UNCHECKED_CAST") val instance = pool[lastPooledIndex] as T

            pool[lastPooledIndex] = null
            size--

            instance
        } else {
            null
        }

    fun release(instance: T) {
        pool[size] = instance
        size++
    }
}
