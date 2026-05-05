package com.example.shared.feature.news.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NewsNavigator(startDestination: NewsDestination = NewsDestination.Feed) {
    private val _backStack = MutableStateFlow(listOf(startDestination))
    val backStack: StateFlow<List<NewsDestination>> = _backStack.asStateFlow()

    fun currentDestination(): NewsDestination = _backStack.value.last()

    fun navigate(destination: NewsDestination) {
        _backStack.value = _backStack.value + destination
    }

    fun popBackStack() {
        if (_backStack.value.size <= 1) return
        _backStack.value = _backStack.value.dropLast(1)
    }
}

