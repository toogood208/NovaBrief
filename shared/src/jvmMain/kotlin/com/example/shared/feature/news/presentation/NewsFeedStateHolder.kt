package com.example.shared.feature.news.presentation

import com.example.shared.feature.news.data.repository.JvmNewsRepository
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import retrofit2.HttpException

class NewsFeedStateHolder(
    private val repository: JvmNewsRepository,
    private val scope: CoroutineScope
) {
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
        observeJob = repository.observeNews(query)
            .onEach { articles ->
                _state.value = _state.value.copy(
                    articles = articles,
                    isInitialLoad = _state.value.isInitialLoad && articles.isEmpty()
                )
            }
            .launchIn(scope)
    }

    private fun refreshNews() {
        _state.value = _state.value.copy(
            isLoading = true,
            errorTitle = null,
            errorMessage = null,
            isShowingCachedContent = false
        )

        scope.launch {
            runCatching {
                repository.refreshNews(activeQuery)
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
                    errorMessage = message,
                    isShowingCachedContent = hasCachedArticles
                )
            }
        }
    }
}

private fun Throwable.toNewsError(hasCachedArticles: Boolean): Pair<String, String> {
    return when (this) {
        is UnknownHostException, is ConnectException -> {
            if (hasCachedArticles) {
                "Offline mode" to "You're offline right now. Showing saved stories until the connection returns."
            } else {
                "No internet connection" to "Connect to the internet to load the latest headlines, then try again."
            }
        }
        is SocketTimeoutException -> {
            "Connection timed out" to "The request took too long. Please retry in a moment."
        }
        is IOException -> {
            "Network error" to (message ?: "Could not reach the news service. Please try again.")
        }
        is HttpException -> {
            when (code()) {
                429 -> "Rate limit reached" to "The news service is temporarily busy. Wait a moment and try again."
                in 500..599 -> "News service unavailable" to "The news server is having trouble. Please retry in a bit."
                else -> "Unable to load articles" to "We couldn't refresh this section right now. Please try again."
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

