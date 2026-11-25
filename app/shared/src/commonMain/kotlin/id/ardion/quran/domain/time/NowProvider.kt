package id.ardion.quran.domain.time

import kotlinx.datetime.LocalDateTime

fun interface NowProvider {
    fun now(): LocalDateTime
}
