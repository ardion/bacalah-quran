package id.ardion.quran.di

import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.data.repository.LastReadRepositoryImpl
import id.ardion.quran.data.repository.PrayerRepositoryImpl
import id.ardion.quran.data.repository.QuranRepositoryImpl
import id.ardion.quran.domain.irepository.LastReadRepository
import id.ardion.quran.domain.irepository.PrayerRepository
import id.ardion.quran.domain.irepository.QuranRepository
import id.ardion.quran.domain.usecase.ClearLastReadUseCase
import id.ardion.quran.domain.usecase.GetLastReadUseCase
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.GetPrayerTimesUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.domain.usecase.SaveLastReadPageUseCase
import id.ardion.quran.domain.usecase.SaveLastReadSurahUseCase
import id.ardion.quran.presentation.home.HomeViewModel
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.prayer.PrayerViewModel
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import id.ardion.quran.utils.CompassSensor
import id.ardion.quran.utils.LocationService
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureQuranModule = module {
    singleOf(::QuranApiService)
    singleOf(::PrayerApiService)
    single { LocationService() }
    single { CompassSensor() }
    singleOf(::QuranRepositoryImpl) bind QuranRepository::class
    singleOf(::PrayerRepositoryImpl) bind PrayerRepository::class
    singleOf(::LastReadRepositoryImpl) bind LastReadRepository::class
    singleOf(::GetSurahListUseCase)
    singleOf(::GetSurahDetailUseCase)
    singleOf(::GetPageDetailUseCase)
    singleOf(::GetPrayerTimesUseCase)
    singleOf(::GetLastReadUseCase)
    singleOf(::SaveLastReadSurahUseCase)
    singleOf(::SaveLastReadPageUseCase)
    singleOf(::ClearLastReadUseCase)
    factoryOf(::HomeViewModel)
    factoryOf(::SurahListViewModel)
    factoryOf(::SurahDetailViewModel)
    factoryOf(::MushafPageViewModel)
    factoryOf(::PrayerViewModel)
}

val appModule = module {
    includes(coreNetworkModule, coreAudioModule, featureQuranModule)
}
