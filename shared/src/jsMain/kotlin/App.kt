import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.shared.feature.news.data.repository.WebNewsRepository
import com.example.shared.feature.news.presentation.NewsDestination
import com.example.shared.feature.news.presentation.NewsDetailScreenUi
import com.example.shared.feature.news.presentation.NewsNavigator
import com.example.shared.feature.news.presentation.NewsScreenUi
import com.example.shared.feature.news.presentation.SharedNewsDetailUiState
import com.example.shared.feature.news.presentation.WebNewsStateHolder
import kotlinx.browser.window
import kotlinx.coroutines.flow.flowOf

@Composable
fun App() {
    val apiKey = remember { resolveInjectedWebApiKey() }

    val repository = remember(apiKey) { WebNewsRepository(apiKey = apiKey) }
    val scope = rememberCoroutineScope()
    val navigator = remember { NewsNavigator() }
    val stateHolder = remember(repository, scope) {
        WebNewsStateHolder(repository = repository, scope = scope)
    }

    val uiState by stateHolder.state.collectAsState()

    val backStack by navigator.backStack.collectAsState()
    val destination = backStack.lastOrNull() ?: NewsDestination.Feed
    val selectedArticleId = (destination as? NewsDestination.Detail)?.articleId

    val selectedArticle by remember(selectedArticleId, repository) {
        selectedArticleId?.let(repository::observeArticleById) ?: flowOf(null)
    }.collectAsState(initial = null)

    MaterialTheme {
        if (destination is NewsDestination.Feed) {
            NewsScreenUi(
                uiState = uiState,
                gridColumns = 4,
                onArticleSelected = { articleId ->
                    navigator.navigate(NewsDestination.Detail(articleId))
                },
                onEvent = stateHolder::onEvent
            )
        } else {
            NewsDetailScreenUi(
                uiState = SharedNewsDetailUiState(
                    article = selectedArticle,
                    isLoading = selectedArticle == null
                ),
                onBack = { navigator.popBackStack() },
                onOpenSource = { url ->
                    window.open(url, "_blank")
                }
            )
        }
    }
}


fun resolveInjectedWebApiKey(): String {
    val global = js("globalThis")
    val globalKey = (global.NEWS_API_KEY as? String).orEmpty().trim()
    if (globalKey.isNotEmpty() && !globalKey.contains("\${")) {
        return globalKey
    }

    return WEB_NEWS_API_KEY.trim()
}

