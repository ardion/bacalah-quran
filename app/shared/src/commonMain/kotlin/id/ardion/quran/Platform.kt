package id.ardion.quran

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform