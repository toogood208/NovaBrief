package com.example.novabrief.feature.news.data.repository

import com.example.novabrief.feature.news.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun getNewsStream(category: String?): Flow<List<NewsArticle>>
    suspend fun refreshNews(category: String?)
}
