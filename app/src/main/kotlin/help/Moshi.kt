package help

import com.squareup.moshi.JsonReader

inline fun JsonReader.forObject(f: JsonReader.(name: String) -> Unit) {
    beginObject()
    while (hasNext()) {
        f(nextName())
    }
    endObject()
}

inline fun JsonReader.forArray(f: JsonReader.() -> Unit) {
    beginArray()
    while (hasNext()) {
        f()
    }
    endArray()
}
