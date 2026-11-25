package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.PageDetail
import id.ardion.quran.domain.repository.QuranRepository

class GetPageDetailUseCase(private val repository: QuranRepository) {
    suspend operator fun invoke(pageNumber: Int): Result<PageDetail> = repository.getPageDetail(pageNumber)
}
