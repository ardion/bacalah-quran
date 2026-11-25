package id.ardion.quran.di

import id.ardion.quran.data.remote.KemenagApiService
import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.data.repository.LastReadRepositoryImpl
import id.ardion.quran.data.repository.PrayerRepositoryImpl
import id.ardion.quran.data.repository.QuranRepositoryImpl
import id.ardion.quran.data.repository.ReminderRepositoryImpl
import id.ardion.quran.domain.repository.LastReadRepository
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.repository.QuranRepository
import id.ardion.quran.domain.repository.ReminderRepository
import id.ardion.quran.domain.time.NowProvider
import id.ardion.quran.domain.time.TodayProvider
import id.ardion.quran.domain.usecase.BuildReminderEventsUseCase
import id.ardion.quran.domain.usecase.ClearMushafLastReadUseCase
import id.ardion.quran.domain.usecase.ClearSurahLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.GetPrayerTimesUseCase
import id.ardion.quran.domain.usecase.GetReminderSettingsUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.domain.usecase.SaveMushafLastReadUseCase
import id.ardion.quran.domain.usecase.SaveReminderSettingsUseCase
import id.ardion.quran.domain.usecase.SaveSurahLastReadUseCase
import id.ardion.quran.domain.usecase.ScheduleRemindersUseCase
import id.ardion.quran.domain.usecase.UpdateReminderLocationUseCase
import id.ardion.quran.presentation.home.HomeViewModel
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.prayer.PrayerViewModel
import id.ardion.quran.presentation.reminder.ReminderSettingsViewModel
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.platform.SystemTodayProvider
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val dataModule = module {
    singleOf(::QuranApiService)
    singleOf(::PrayerApiService)
    singleOf(::KemenagApiService)
    single { SystemTodayProvider() }
    single<TodayProvider> { get<SystemTodayProvider>() }
    single<NowProvider> { get<SystemTodayProvider>() }
    singleOf(::ReminderRepositoryImpl) { bind<ReminderRepository>() }
    singleOf(::QuranRepositoryImpl) { bind<QuranRepository>() }
    singleOf(::PrayerRepositoryImpl) { bind<PrayerRepository>() }
    singleOf(::LastReadRepositoryImpl) { bind<LastReadRepository>() }
}

private val domainModule = module {
    factoryOf(::GetSurahListUseCase)
    factoryOf(::GetSurahDetailUseCase)
    factoryOf(::GetPageDetailUseCase)
    factoryOf(::GetPrayerTimesUseCase)
    factoryOf(::GetLastReadUseCase)
    factoryOf(::SaveSurahLastReadUseCase)
    factoryOf(::SaveMushafLastReadUseCase)
    factoryOf(::ClearSurahLastReadUseCase)
    factoryOf(::ClearMushafLastReadUseCase)
    factoryOf(::BuildReminderEventsUseCase)
    factoryOf(::ScheduleRemindersUseCase)
    factoryOf(::GetReminderSettingsUseCase)
    factoryOf(::SaveReminderSettingsUseCase)
    factoryOf(::UpdateReminderLocationUseCase)
}

private val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SurahListViewModel)
    viewModelOf(::SurahDetailViewModel)
    viewModelOf(::MushafPageViewModel)
    viewModelOf(::PrayerViewModel)
    viewModelOf(::ReminderSettingsViewModel)
}

val appModule = module {
    includes(coreNetworkModule, coreAudioModule, platformModule, dataModule, domainModule, presentationModule)
}
