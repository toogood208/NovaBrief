package com.example.novabrief.feature.news.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String,
    val articleUrl: String,
    val source: String,
    val publishedAt: String,
    val category: String,
    val cachedAt: Long = System.currentTimeMillis()
)
