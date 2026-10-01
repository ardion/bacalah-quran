package id.ardion.quran.di

import id.ardion.quran.data.remote.QuranApiService
import id.ardion.quran.data.repository.QuranRepositoryImpl
import id.ardion.quran.domain.irepository.QuranRepository
import id.ardion.quran.domain.usecase.GetPageDetailUseCase
import id.ardion.quran.domain.usecase.GetSurahDetailUseCase
import id.ardion.quran.domain.usecase.GetSurahListUseCase
import id.ardion.quran.presentation.mushaf.MushafPageViewModel
import id.ardion.quran.presentation.surah.SurahDetailViewModel
import id.ardion.quran.presentation.surahlist.SurahListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val featureQuranModule = module {
    singleOf(::QuranApiService)
    singleOf(::QuranRepositoryImpl) bind QuranRepository::class
    singleOf(::GetSurahListUseCase)
    singleOf(::GetSurahDetailUseCase)
    singleOf(::GetPageDetailUseCase)
    factoryOf(::SurahListViewModel)
    factoryOf(::SurahDetailViewModel)
    factoryOf(::MushafPageViewModel)
}

val appModule = module {
    includes(coreNetworkModule, coreAudioModule, featureQuranModule)
}
