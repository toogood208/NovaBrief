package com.example.shared.feature.news.data.repository

import com.example.shared.feature.news.data.local.SharedNewsDatabase
import com.example.shared.feature.news.data.mapper.NewsApiArticleInput
import com.example.shared.feature.news.data.mapper.isTopHeadlinesCategory
import com.example.shared.feature.news.data.mapper.isTopStoriesCategory
import com.example.shared.feature.news.data.mapper.mapApiArticles
import com.example.shared.feature.news.data.mapper.normalizeRequestedCategory
import com.example.shared.feature.news.data.remote.NewsApiService
import com.example.shared.feature.news.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow

class JvmNewsRepository(
    private val apiService: NewsApiService,
    private val localDatabase: SharedNewsDatabase,
    private val apiKey: String
) {
    fun observeNews(category: String?): Flow<List<NewsArticle>> {
        val dbCategory = normalizeRequestedCategory(category)
        return localDatabase.observeArticlesByCategory(dbCategory)
    }

    fun observeArticleById(articleId: String): Flow<NewsArticle?> {
        return localDatabase.observeArticleById(articleId)
    }

    suspend fun refreshNews(category: String?) {
        require(apiKey.isNotBlank()) {
            "Missing NEWS_API_KEY. Set NEWS_API_KEY in your environment or local.properties before app launch."
        }

        val normalizedCategory = category?.lowercase()?.trim()
        val dbCategory = normalizeRequestedCategory(category)

        val response = when {
            isTopStoriesCategory(normalizedCategory) -> {
                apiService.getTopHeadlines(apiKey = apiKey, country = "us")
            }

            isTopHeadlinesCategory(normalizedCategory) -> {
                apiService.getTopHeadlines(apiKey = apiKey, category = normalizedCategory)
            }

            else -> {
                apiService.getEverything(
                    apiKey = apiKey,
                    query = normalizedCategory.orEmpty(),
                    excludeDomains = "consent.yahoo.com"
                )
            }
        }

        val articles = mapApiArticles(
            articles = response.articles.map { dto ->
                NewsApiArticleInput(
                    title = dto.title,
                    description = dto.description,
                    content = dto.content,
                    url = dto.url,
                    urlToImage = dto.urlToImage,
                    sourceName = dto.source?.name,
                    publishedAt = dto.publishedAt
                )
            },
            requestedCategory = normalizedCategory
        )

        if (articles.isNotEmpty()) {
            localDatabase.replaceCategory(dbCategory, articles)
        }
    }
}

