package com.example.shared.feature.news.repository

import com.example.shared.feature.news.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNewsStream(category: String?): Flow<List<NewsArticle>>
    suspend fun refreshNews(category: String?)
}
