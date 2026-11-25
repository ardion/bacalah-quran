package id.ardion.quran.domain.time

import kotlinx.datetime.LocalDate

fun interface TodayProvider {
    fun today(): LocalDate
}
