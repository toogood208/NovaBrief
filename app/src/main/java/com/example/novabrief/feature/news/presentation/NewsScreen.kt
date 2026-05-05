package com.example.novabrief.feature.news.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shared.feature.news.presentation.NewsScreenUi
import com.example.shared.feature.news.presentation.NewsUiEvent as SharedNewsUiEvent

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    onArticleSelected: (String) -> Unit = {},
    onSelectInterests: () -> Unit = {},
    viewModel: NewsViewModel = viewModel()
) {
    val uiState by viewModel.state.collectAsState()

    NewsScreenUi(
        uiState = uiState,
        modifier = modifier,
        onArticleSelected = onArticleSelected,
        onSelectInterests = onSelectInterests,
        onEvent = { event ->
            // Map shared event → local Android event → viewModel
            when (event) {
                is SharedNewsUiEvent.OnRefresh ->
                    viewModel.onEvent(NewsUiEvent.OnRefresh)
                is SharedNewsUiEvent.OnCategoryChanged ->
                    viewModel.onEvent(NewsUiEvent.OnCategoryChanged(event.category))
            }
        }
    )
}