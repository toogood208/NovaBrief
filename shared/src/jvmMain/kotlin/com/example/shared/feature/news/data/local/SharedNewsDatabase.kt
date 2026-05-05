package com.example.shared.feature.news.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.db.SqlDriver
import com.example.shared.feature.news.data.local.db.News_articles
import com.example.shared.feature.news.data.local.db.NovaBriefDatabase
import com.example.shared.feature.news.domain.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SharedNewsDatabase(driver: SqlDriver) {
    private val database = NovaBriefDatabase(driver)
    private val queries = database.newsArticlesQueries

    fun observeArticlesByCategory(category: String): Flow<List<NewsArticle>> {
        return queries.selectByCategory(category)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map(News_articles::toDomain) }
    }

    fun observeArticleById(id: String): Flow<NewsArticle?> {
        return queries.selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { row -> row?.toDomain() }
    }

    suspend fun replaceCategory(category: String, articles: List<NewsArticle>) {
        withContext(Dispatchers.Default) {
            database.transaction {
                queries.deleteByCategory(category)
                articles.forEach { article ->
                    queries.upsertArticle(
                        id = article.id,
                        title = article.title,
                        summary = article.summary,
                        content = article.content,
                        imageUrl = article.imageUrl,
                        articleUrl = article.articleUrl,
                        source = article.source,
                        publishedAt = article.publishedAt,
                        category = category
                    )
                }
            }
        }
    }
}

private fun News_articles.toDomain(): NewsArticle {
    return NewsArticle(
        id = id,
        title = title,
        summary = summary,
        content = content,
        imageUrl = imageUrl,
        articleUrl = articleUrl,
        source = source,
        publishedAt = publishedAt,
        category = category
    )
}

