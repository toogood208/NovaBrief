package com.example.novabrief.feature.news.presentation

typealias NewsUiState = com.example.shared.feature.news.presentation.NewsUiState

sealed interface NewsUiEvent {
    data object OnRefresh : NewsUiEvent
    data class OnCategoryChanged(val category: String) : NewsUiEvent
}


//data class NewsUiState(
//    val isLoading: Boolean = false,
//    val isInitialLoad: Boolean = true,
//    val articles: List<NewsArticle> = emptyList(),
//    val errorTitle: String? = null,
//    val isShowingCachedContent: Boolean = false,
//    val errorMessage: String? = null
//)

//sealed interface NewsUiEvent {
//    data object OnRefresh : NewsUiEvent
//    data class OnCategoryChanged(val category: String) : NewsUiEvent
//}

//sealed interface NewsUiEffect {
//    data class ShowMessage(val message: String) : NewsUiEffect
//}
