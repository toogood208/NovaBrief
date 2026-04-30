package com.example.novabrief.feature.news.data

import androidx.compose.ui.graphics.Color

data class NewsFeedArticle(
    val id: String,
    val category: String,
    val source: String,
    val title: String,
    val summary: String,
    val timeAgo: String,
    val imageUrl: String = "",
    val imageColor: Color
)

val newsCategories = listOf("Top Stories", "Business", "Technology", "World", "Sports")
