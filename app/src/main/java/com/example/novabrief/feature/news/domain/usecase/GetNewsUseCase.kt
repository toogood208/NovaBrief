package com.example.novabrief.feature.news.domain.usecase

import com.example.novabrief.feature.news.data.repository.NewsRepository
import com.example.novabrief.feature.news.domain.model.NewsArticle
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
