package com.example.novabrief.feature.news.domain.model

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String,
    val imageUrl: String,
    val articleUrl: String,
    val source: String,
    val publishedAt: String,
    val category: String
)
