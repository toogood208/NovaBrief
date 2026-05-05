package com.example.shared.feature.news.data.mapper

import com.example.shared.feature.news.domain.model.NewsArticle

const val CATEGORY_TOP_STORIES = "top_stories"

private val blockedUrlParts = listOf(
    "consent.yahoo.com",
    "sessionId=",
    "/collectConsent"
)

data class NewsApiArticleInput(
    val title: String?,
    val description: String?,
    val content: String?,
    val url: String?,
    val urlToImage: String?,
    val sourceName: String?,
    val publishedAt: String?
)

fun mapApiArticles(
    articles: List<NewsApiArticleInput>,
    requestedCategory: String?
): List<NewsArticle> {
    val normalizedCategory = requestedCategory?.lowercase()?.trim()

    return articles.asSequence()
        .mapNotNull { article ->
            val title = article.title?.trim().orEmpty()
            val url = article.url?.trim().orEmpty()
            val summary = article.description?.trim().orEmpty().ifBlank {
                article.content.cleanContentFragment()
            }
            val cleanContent = article.content.cleanContentFragment()

            if (title.isBlank() || url.isBlank()) return@mapNotNull null
            if (!url.startsWith("http", ignoreCase = true)) return@mapNotNull null
            if (blockedUrlParts.any { blocked -> url.contains(blocked, ignoreCase = true) }) return@mapNotNull null
            if (title.equals("[removed]", ignoreCase = true)) return@mapNotNull null

            NewsArticle(
                id = url,
                title = title,
                summary = summary,
                content = cleanContent,
                imageUrl = article.urlToImage.orEmpty(),
                articleUrl = url,
                source = article.sourceName.orEmpty().ifBlank { "Unknown Source" },
                publishedAt = article.publishedAt.orEmpty(),
                category = normalizedCategory ?: classifyCategory(title, article.description.orEmpty())
            )
        }
        .distinctBy { it.id }
        .toList()
}

fun normalizeRequestedCategory(category: String?): String {
    return category?.lowercase()?.trim() ?: CATEGORY_TOP_STORIES
}

fun isTopStoriesCategory(category: String?): Boolean {
    return category.isNullOrBlank() || category == CATEGORY_TOP_STORIES
}

fun isTopHeadlinesCategory(category: String?): Boolean {
    return category in setOf(
        "business",
        "entertainment",
        "general",
        "health",
        "science",
        "sports",
        "technology"
    )
}

fun classifyCategory(title: String, description: String): String {
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

private fun String?.cleanContentFragment(): String {
    return this?.trim().orEmpty().substringBefore("[").trim()
}

