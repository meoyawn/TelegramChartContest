package help

import com.squareup.moshi.JsonReader

inline fun JsonReader.array(f: JsonReader.() -> Unit) {
    beginArray()
    f(this)
    endArray()
}

/**
 * iterates over a json object
 */
inline fun JsonReader.forEachKey(f: (name: String, reader: JsonReader) -> Unit) {
    beginObject()
    while (hasNext()) {
        f(nextName(), this)
    }
    endObject()
}

/**
 * iterates over a json array
 */
inline fun JsonReader.forEach(f: (JsonReader) -> Unit) {
    beginArray()
    while (hasNext()) {
        f(this)
    }
    endArray()
}
