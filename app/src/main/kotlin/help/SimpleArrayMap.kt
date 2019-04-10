package help

import android.util.SparseArray
import androidx.collection.SimpleArrayMap

fun <K, V> SimpleArrayMap<K, V>.first(): V =
    valueAt(0)

fun <K, V> SimpleArrayMap<K, V>.last(): V =
    valueAt(size() - 1)

inline fun <K, V> SimpleArrayMap<K, V>.forEach(f: (K, V) -> Unit) {
    for (i in 0 until size()) {
        f(keyAt(i), valueAt(i))
    }
}

inline fun <V> SparseArray<V>.forEach(f: (Int, V) -> Unit) {
    for (i in 0 until size()) {
        f(keyAt(i), valueAt(i))
    }
}

inline fun <K, V> SimpleArrayMap<K, V>.forEachKey(f: (K) -> Unit) {
    for (i in 0 until size()) {
        f(keyAt(i))
    }
}

inline fun <K, V> SimpleArrayMap<K, V>.forEachValue(f: (V) -> Unit) {
    for (i in 0 until size()) {
        f(valueAt(i))
    }
}

inline fun <K, V> SimpleArrayMap<K, V>.filterValues(f: (K, V) -> Boolean): List<V> =
    ArrayList<V>().also { list ->
        forEach { k, v ->
            if (f(k, v)) {
                list += v
            }
        }
    }

inline fun <K, V> SimpleArrayMap<K, V>.filterKeys(f: (K, V) -> Boolean): List<K> =
    ArrayList<K>().also { list ->
        forEach { k, v ->
            if (f(k, v)) {
                list += k
            }
        }
    }

inline fun <K, V> SimpleArrayMap<K, V>.findKey(f: (K, V) -> Boolean): K? {
    forEach { k, v ->
        if (f(k, v)) {
            return k
        }
    }
    return null
}

inline fun <K, V> SimpleArrayMap<K, V>.any(f: (K, V) -> Boolean): Boolean =
    findKey(f) != null

operator fun <K, V> SimpleArrayMap<K, V>.set(k: K, v: V): V? =
    put(k, v)

operator fun <K, V> SimpleArrayMap<K, V>.minusAssign(k: K) {
    remove(k)
}

inline fun <K, V1, V2> SimpleArrayMap<K, V1>.mapValues(f: (K, V1) -> V2): SimpleArrayMap<K, V2> =
    SimpleArrayMap<K, V2>(size()).also { new ->
        forEach { k, v ->
            new[k] = f(k, v)
        }
    }

inline fun <K, V> List<K>.toSimpleArrayMap(f: (K) -> V): SimpleArrayMap<K, V> =
    SimpleArrayMap<K, V>(size).also { new ->
        forEachByIndex { k ->
            new[k] = f(k)
        }
    }
