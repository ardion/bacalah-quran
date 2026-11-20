package id.ardion.quran.navigation

import kotlinx.serialization.Serializable

sealed interface NavRoute {

    @Serializable
    data object Dashboard : NavRoute

    @Serializable
    data object SurahList : NavRoute

    @Serializable
    data object MushafPage : NavRoute

    @Serializable
    data object PrayerTimes : NavRoute

    @Serializable
    data class SurahDetail(val surahNumber: Int) : NavRoute
}
