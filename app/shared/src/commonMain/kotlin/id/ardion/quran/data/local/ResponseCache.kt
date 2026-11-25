package id.ardion.quran.data.local

interface ResponseCache {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun remove(key: String)
}
