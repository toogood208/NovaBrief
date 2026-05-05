package com.example.shared.feature.news.domain.usecase

import com.example.shared.feature.news.domain.model.NewsArticle
import com.example.shared.feature.news.repository.NewsRepository
import kotlinx.coroutines.flow.Flow

class GetNewsUseCase(
    private val repository: NewsRepository
) {
    operator fun invoke(category: String?): Flow<List<NewsArticle>> {
        return repository.getNewsStream(category)
    }

    suspend fun refresh(category: String?) {
        repository.refreshNews(category)
    }
}
