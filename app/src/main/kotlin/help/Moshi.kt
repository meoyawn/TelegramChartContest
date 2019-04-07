package help

import com.squareup.moshi.JsonReader

inline fun JsonReader.loop(until: JsonReader.Token, f: (JsonReader) -> Unit) {
    while (peek() != until) {
        f(this)
    }
}

inline fun JsonReader.array(f: (JsonReader) -> Unit) {
    beginArray()
    f(this)
    endArray()
}

/**
 * iterates over a json object
 */
inline fun JsonReader.forEachKey(f: (name: String, reader: JsonReader) -> Unit) {
    beginObject()
    loop(until = JsonReader.Token.END_OBJECT) {
        f(nextName(), this)
    }
    endObject()
}

/**
 * iterates over a json array
 */
inline fun JsonReader.forEach(f: (JsonReader) -> Unit) {
    beginArray()
    loop(until = JsonReader.Token.END_ARRAY, f = f)
    endArray()
}
