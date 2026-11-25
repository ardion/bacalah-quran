package id.ardion.quran.di

import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.data.repository.LastReadRepositoryImpl
import id.ardion.quran.data.repository.PrayerRepositoryImpl
import id.ardion.quran.data.repository.QuranRepositoryImpl
import id.ardion.quran.domain.repository.LastReadRepository
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.domain.repository.QuranRepository
import id.ardion.quran.domain.usecase.ClearMushafLastReadUseCase
import id.ardion.quran.domain.usecase.ClearSurahLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.GetPrayerTimesUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.domain.usecase.SaveMushafLastReadUseCase
import id.ardion.quran.domain.usecase.SaveSurahLastReadUseCase
import id.ardion.quran.presentation.home.HomeViewModel
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.prayer.PrayerViewModel
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

private val dataModule = module {
    singleOf(::QuranApiService)
    singleOf(::PrayerApiService)
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
}

private val presentationModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::SurahListViewModel)
    viewModelOf(::SurahDetailViewModel)
    viewModelOf(::MushafPageViewModel)
    viewModelOf(::PrayerViewModel)
}

val appModule = module {
    includes(coreNetworkModule, coreAudioModule, platformModule, dataModule, domainModule, presentationModule)
}
