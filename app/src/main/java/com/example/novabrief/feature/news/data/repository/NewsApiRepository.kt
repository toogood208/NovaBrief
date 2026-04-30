package com.example.novabrief.feature.news.data.repository

import com.example.novabrief.feature.news.data.local.NewsDao
import com.example.novabrief.feature.news.data.remote.NewsApiService
import com.example.novabrief.feature.news.domain.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsApiRepository(
    private val apiService: NewsApiService,
    private val newsDao: NewsDao,
    private val apiKey: String
) : NewsRepository {

    companion object {
        const val CATEGORY_TOP_STORIES = "top_stories"
    }

    private val blockedUrlParts = listOf(
        "consent.yahoo.com",
        "sessionId=",
        "/collectConsent"
    )

    override fun getNewsStream(category: String?): Flow<List<NewsArticle>> {
        val dbCategory = category?.lowercase()?.trim() ?: CATEGORY_TOP_STORIES
        return newsDao.getArticlesByCategory(dbCategory).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshNews(category: String?) {
        require(apiKey.isNotBlank()) {
            "Missing NEWS_API_KEY. Add NEWS_API_KEY=your_key in local.properties"
        }

        val topHeadlinesCategories = listOf("business", "entertainment", "general", "health", "science", "sports", "technology")
        val normalizedCategory = category?.lowercase()?.trim()
        val dbCategory = normalizedCategory ?: CATEGORY_TOP_STORIES

        val response = if (normalizedCategory.isNullOrBlank() || normalizedCategory == CATEGORY_TOP_STORIES) {
            apiService.getTopHeadlines(apiKey = apiKey, country = "us")
        } else if (topHeadlinesCategories.contains(normalizedCategory)) {
            apiService.getTopHeadlines(apiKey = apiKey, category = normalizedCategory)
        } else {
            apiService.getEverything(
                apiKey = apiKey,
                query = normalizedCategory,
                excludeDomains = "consent.yahoo.com"
            )
        }

        val articles = response.articles
            .asSequence()
            .mapNotNull { dto ->
                val title = dto.title?.trim().orEmpty()
                val url = dto.url?.trim().orEmpty()
                val summary = dto.description?.trim().orEmpty().ifBlank {
                    dto.content?.trim().orEmpty().substringBefore("[").trim()
                }

                if (title.isBlank() || url.isBlank()) return@mapNotNull null
                if (!url.startsWith("http", ignoreCase = true)) return@mapNotNull null
                if (blockedUrlParts.any { blocked -> url.contains(blocked, ignoreCase = true) }) return@mapNotNull null
                if (title.equals("[removed]", ignoreCase = true)) return@mapNotNull null

                NewsArticle(
                    id = url,
                    title = title,
                    summary = summary,
                    content = dto.content?.trim().orEmpty().substringBefore("[").trim(),
                    imageUrl = dto.urlToImage.orEmpty(),
                    articleUrl = url,
                    source = dto.source?.name.orEmpty().ifBlank { "Unknown Source" },
                    publishedAt = dto.publishedAt.orEmpty(),
                    category = normalizedCategory ?: classifyCategory(title, dto.description.orEmpty())
                )
            }
            .distinctBy { it.id }
            .toList()

        // Cache the new articles
        if (articles.isNotEmpty()) {
            newsDao.deleteArticlesByCategory(dbCategory)
            newsDao.insertArticles(articles.map { it.toEntity(dbCategory) })
        }
    }

    private fun classifyCategory(title: String, description: String): String {
        val text = ("$title $description").lowercase()
        return when {
            text.contains("market") || text.contains("stock") || text.contains("business") || text.contains("econom") -> "business"
            text.contains("match") || text.contains("league") || text.contains("goal") || text.contains("tournament") -> "sports"
            text.contains("election") || text.contains("senate") || text.contains("government") || text.contains("president") -> "politics"
            text.contains("health") || text.contains("hospital") || text.contains("vaccine") || text.contains("doctor") -> "health"
            text.contains("space") || text.contains("science") || text.contains("research") || text.contains("nasa") -> "science"
            text.contains("travel") || text.contains("flight") || text.contains("tourism") || text.contains("destination") -> "travel"
            else -> "world"
        }
    }
}
