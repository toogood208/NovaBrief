package com.example.novabrief.feature.news.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.novabrief.BuildConfig
import com.example.novabrief.feature.news.data.local.NewsDatabase
import com.example.novabrief.feature.news.data.remote.NewsApiClient
import com.example.novabrief.feature.news.data.repository.NewsApiRepository
import com.example.novabrief.feature.news.domain.usecase.GetNewsUseCase
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = NewsDatabase.getInstance(application)
    private val getNewsUseCase: GetNewsUseCase = GetNewsUseCase(
        repository = NewsApiRepository(
            apiService = NewsApiClient.service,
            newsDao = database.dao,
            apiKey = BuildConfig.NEWS_API_KEY,
        )
    )

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    private var activeQuery: String? = null
    private var observeJob: Job? = null

    init {
        observeCategory(null)
        refreshNews()
    }

    fun onEvent(event: NewsUiEvent) {
        when (event) {
            NewsUiEvent.OnRefresh -> refreshNews()
            is NewsUiEvent.OnCategoryChanged -> {
                val newQuery = mapUiCategoryToQuery(event.category)
                if (newQuery != activeQuery) {
                    activeQuery = newQuery
                    observeCategory(newQuery)
                    refreshNews()
                }
            }
        }
    }

    private fun observeCategory(query: String?) {
        observeJob?.cancel()
        observeJob = getNewsUseCase(query)
            .onEach { articles ->
                _state.value = _state.value.copy(
                    articles = articles,
                    isInitialLoad = _state.value.isInitialLoad && articles.isEmpty()
                )
            }
            .launchIn(viewModelScope)
    }

    private fun refreshNews() {
        _state.value = _state.value.copy(
            isLoading = true,
            errorTitle = null,
            errorMessage = null,
            isShowingCachedContent = false
        )
        viewModelScope.launch {
            runCatching {
                getNewsUseCase.refresh(activeQuery)
            }.onSuccess {
                _state.value = _state.value.copy(
                    isLoading = false,
                    isInitialLoad = false,
                    errorTitle = null,
                    errorMessage = null,
                    isShowingCachedContent = false
                )
            }.onFailure { throwable ->
                val hasCachedArticles = _state.value.articles.isNotEmpty()
                val (title, message) = throwable.toNewsError(hasCachedArticles)

                _state.value = _state.value.copy(
                    isLoading = false,
                    isInitialLoad = false,
                    errorTitle = title,
                    isShowingCachedContent = hasCachedArticles,
                    errorMessage = message
                )
            }
        }
    }

    private fun mapUiCategoryToQuery(category: String): String? {
        return when (category.lowercase()) {
            "top stories" -> NewsApiRepository.CATEGORY_TOP_STORIES
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
}

private fun Throwable.toNewsError(hasCachedArticles: Boolean): Pair<String, String> {
    return when (this) {
        is IOException -> {
            if (hasCachedArticles) {
                "Offline mode" to "You're offline right now. Showing saved stories until the connection returns."
            } else {
                "No internet connection" to "Connect to the internet to load the latest headlines, then try again."
            }
        }
        is HttpException -> {
            when (code()) {
                429 -> {
                    "Rate limit reached" to "The news service is temporarily busy. Wait a moment and try again."
                }
                in 500..599 -> {
                    "News service unavailable" to "The news server is having trouble. Please retry in a bit."
                }
                else -> {
                    "Unable to load articles" to "We couldn't refresh this section right now. Please try again."
                }
            }
        }
        is IllegalArgumentException -> {
            "Configuration issue" to (message ?: "The app is missing required configuration for news loading.")
        }
        else -> {
            "Unable to load articles" to (message ?: "We couldn't refresh this section right now. Please try again.")
        }
    }
}
