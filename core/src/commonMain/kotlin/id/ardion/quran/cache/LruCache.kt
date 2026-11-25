package id.ardion.quran.cache

class LruCache<K, V>(private val maxSize: Int) {
    private val entries = LinkedHashMap<K, V>()

    operator fun get(key: K): V? {
        val value = entries.remove(key) ?: return null
        entries[key] = value
        return value
    }

    operator fun set(key: K, value: V) {
        entries.remove(key)
        entries[key] = value
        if (entries.size > maxSize) {
            entries.keys.firstOrNull()?.let(entries::remove)
        }
    }

    val size: Int get() = entries.size
}
