package com.example.novabrief.feature.news.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.novabrief.BuildConfig
import com.example.novabrief.feature.news.data.local.SharedNewsDatabaseProvider
import com.example.shared.feature.news.data.remote.NewsApiClient
import com.example.shared.feature.news.data.repository.JvmNewsRepository
import com.example.shared.feature.news.presentation.NewsFeedStateHolder
import kotlinx.coroutines.flow.StateFlow

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val stateHolder = NewsFeedStateHolder(
        repository = JvmNewsRepository(
            apiService = NewsApiClient.service,
            localDatabase = SharedNewsDatabaseProvider.getInstance(application),
            apiKey = BuildConfig.NEWS_API_KEY
        ),
        scope = viewModelScope
    )

    val state: StateFlow<NewsUiState> = stateHolder.state

    fun onEvent(event: NewsUiEvent) {
        stateHolder.onEvent(
            when (event) {
                NewsUiEvent.OnRefresh -> com.example.shared.feature.news.presentation.NewsUiEvent.OnRefresh
                is NewsUiEvent.OnCategoryChanged -> com.example.shared.feature.news.presentation.NewsUiEvent.OnCategoryChanged(event.category)
            }
        )
    }
}
