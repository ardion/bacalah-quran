package id.ardion.quran.platform

import id.ardion.quran.domain.time.TodayProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SystemTodayProvider : TodayProvider {
    @OptIn(ExperimentalTime::class)
    override fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
