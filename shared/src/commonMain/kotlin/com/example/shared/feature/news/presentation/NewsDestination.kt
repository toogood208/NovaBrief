package com.example.shared.feature.news.presentation

sealed interface NewsDestination {
    data object Feed : NewsDestination
    data class Detail(val articleId: String) : NewsDestination
}

