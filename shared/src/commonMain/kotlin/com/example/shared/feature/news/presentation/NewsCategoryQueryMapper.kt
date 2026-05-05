package com.example.shared.feature.news.presentation

import com.example.shared.feature.news.data.mapper.CATEGORY_TOP_STORIES

fun mapUiCategoryToQuery(category: String): String? {
    return when (category.lowercase()) {
        "top stories" -> CATEGORY_TOP_STORIES
        "all" -> null
        "sports" -> "sports"
        "business" -> "business"
        "technology" -> "technology"
        "world" -> "world"
        "health" -> "health"
        "science" -> "science"
        "politics" -> "politics"
        "travel" -> "travel"
        else -> category
    }
}

