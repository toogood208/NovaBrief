package com.example.shared.feature.news.data.repository

import com.example.shared.feature.news.data.mapper.NewsApiArticleInput
import com.example.shared.feature.news.data.mapper.isTopHeadlinesCategory
import com.example.shared.feature.news.data.mapper.isTopStoriesCategory
import com.example.shared.feature.news.data.mapper.mapApiArticles
import com.example.shared.feature.news.data.mapper.normalizeRequestedCategory
import com.example.shared.feature.news.domain.model.NewsArticle
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WebNewsRepository(
    private val apiKey: String
) {
    private val lock = Mutex()
    private val cache = MutableStateFlow<List<NewsArticle>>(emptyList())

    fun observeNews(category: String?): Flow<List<NewsArticle>> {
        val dbCategory = normalizeRequestedCategory(category)
        return cache.map { articles ->
            articles.filter { it.category == dbCategory }
        }
    }

    fun observeArticleById(articleId: String): Flow<NewsArticle?> {
        return cache.map { articles -> articles.firstOrNull { it.id == articleId } }
    }

    suspend fun refreshNews(category: String?) {

        val normalizedCategory = category?.lowercase()?.trim()
        val dbCategory = normalizeRequestedCategory(category)
        val url = buildApiUrl(normalizedCategory, apiKey)
        val dynamicResponse = fetchJson(url)

        val dynamicArticles = (dynamicResponse.articles as? Array<dynamic>).orEmpty()
        val articles = mapApiArticles(
            articles = dynamicArticles.map { dto ->
                NewsApiArticleInput(
                    title = dto.title as? String,
                    description = dto.description as? String,
                    content = dto.content as? String,
                    url = dto.url as? String,
                    urlToImage = dto.urlToImage as? String,
                    sourceName = dto.source?.name as? String,
                    publishedAt = dto.publishedAt as? String
                )
            },
            requestedCategory = normalizedCategory
        )

        if (articles.isNotEmpty()) {
            lock.withLock {
                val merged = (cache.value.filterNot { it.category == dbCategory } +
                    articles.map { it.copy(category = dbCategory) })
                    .associateBy { it.id }
                    .values
                    .toList()
                cache.value = merged
            }
        }
    }
}

private fun buildApiUrl(category: String?, apiKey: String): String {
    val encodedCategory = category.orEmpty()
    val topStories = isTopStoriesCategory(category)
    val topHeadlines = isTopHeadlinesCategory(category)

    val pathAndQuery = when {
        topStories -> "v2/top-headlines?country=us&pageSize=20"
        topHeadlines -> "v2/top-headlines?country=us&category=$encodedCategory&pageSize=20"
        else -> "v2/everything?q=$encodedCategory&language=en&searchIn=title,description&excludeDomains=consent.yahoo.com&sortBy=publishedAt&pageSize=20"
    }

    return "https://newsapi.org/$pathAndQuery&apiKey=$apiKey"
}

private suspend fun fetchJson(url: String): dynamic {
    val response = window.fetch(url).await()
    val text = response.text().await()
    val payload = runCatching { js("JSON.parse(text)") }.getOrNull()

    if (!response.ok) {
        val apiMessage = payload?.message as? String
        throw IllegalStateException(
            apiMessage ?: "News API request failed (${response.status.toInt()})."
        )
    }

    val status = payload?.status as? String
    if (status == "error") {
        val apiMessage = payload.message as? String
        throw IllegalStateException(apiMessage ?: "News API returned an error response.")
    }

    return payload ?: error("News API returned malformed JSON.")
}

