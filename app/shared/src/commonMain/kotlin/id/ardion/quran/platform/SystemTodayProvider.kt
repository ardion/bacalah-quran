package id.ardion.quran.platform

import id.ardion.quran.domain.time.NowProvider
import id.ardion.quran.domain.time.TodayProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SystemTodayProvider : TodayProvider, NowProvider {
    @OptIn(ExperimentalTime::class)
    override fun now(): LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    override fun today(): LocalDate = now().date
}
