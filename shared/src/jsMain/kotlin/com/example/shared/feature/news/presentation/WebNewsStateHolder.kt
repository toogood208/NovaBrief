package com.example.shared.feature.news.presentation

import com.example.shared.feature.news.data.repository.WebNewsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class WebNewsStateHolder(
    private val repository: WebNewsRepository,
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
                _state.value = _state.value.copy(
                    isLoading = false,
                    isInitialLoad = false,
                    errorTitle = "Unable to load articles",
                    errorMessage = throwable.message ?: "Could not fetch latest stories.",
                    isShowingCachedContent = hasCachedArticles
                )
            }
        }
    }
}

