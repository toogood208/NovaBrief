package com.example.novabrief.feature.news.data.repository

import com.example.novabrief.feature.news.data.local.NewsArticleEntity
import com.example.novabrief.feature.news.domain.model.NewsArticle

fun NewsArticle.toEntity(category: String): NewsArticleEntity {
    return NewsArticleEntity(
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

fun NewsArticleEntity.toDomain(): NewsArticle {
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
