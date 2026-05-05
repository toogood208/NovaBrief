import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.shared.feature.news.data.local.createSharedNewsDatabase
import com.example.shared.feature.news.data.remote.NewsApiClient
import com.example.shared.feature.news.data.repository.JvmNewsRepository
import com.example.shared.feature.news.presentation.NewsDetailScreenUi
import com.example.shared.feature.news.presentation.NewsDestination
import com.example.shared.feature.news.presentation.NewsFeedStateHolder
import com.example.shared.feature.news.presentation.NewsNavigator
import com.example.shared.feature.news.presentation.NewsScreenUi
import com.example.shared.feature.news.presentation.SharedNewsDetailUiState
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.util.Properties
import kotlinx.coroutines.flow.flowOf

@Composable
fun App() {
    val apiKey = remember { resolveDesktopApiKey() }
    val localDatabase = remember { createSharedNewsDatabase(resolveDesktopCacheFile()) }
    val repository = remember(apiKey) {
        JvmNewsRepository(
            apiService = NewsApiClient.service,
            localDatabase = localDatabase,
            apiKey = apiKey
        )
    }
    val scope = rememberCoroutineScope()
    val navigator = remember { NewsNavigator() }
    val stateHolder = remember(repository, scope) {
        NewsFeedStateHolder(repository = repository, scope = scope)
    }

    val uiState by stateHolder.state.collectAsState()

    val backStack by navigator.backStack.collectAsState()
    val destination = backStack.lastOrNull() ?: NewsDestination.Feed
    val selectedArticleId = (destination as? NewsDestination.Detail)?.articleId

    val selectedArticle by remember(selectedArticleId, localDatabase) {
        selectedArticleId?.let(localDatabase::observeArticleById) ?: flowOf(null)
    }.collectAsState(initial = null)

    MaterialTheme {
        if (destination is NewsDestination.Feed) {
            NewsScreenUi(
                uiState = uiState,
                gridColumns = 4,
                onArticleSelected = { articleId -> navigator.navigate(NewsDestination.Detail(articleId)) },
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
                    runCatching {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(URI(url))
                        }
                    }
                }
            )
        }
    }
}

private fun resolveDesktopApiKey(): String {
    val envValue = System.getenv("NEWS_API_KEY").orEmpty().trim()
    if (envValue.isNotEmpty()) return envValue

    return findLocalProperties()
        ?.inputStream()
        ?.use { stream ->
            Properties().apply { load(stream) }
                .getProperty("NEWS_API_KEY")
                .orEmpty()
                .trim()
        }
        .orEmpty()
}

private fun findLocalProperties(): File? {
    var current: File? = File(System.getProperty("user.dir"))
    repeat(6) {
        val candidate = current?.resolve("local.properties")
        if (candidate?.isFile == true) return candidate
        current = current?.parentFile
    }
    return null
}

private fun resolveDesktopCacheFile(): File {
    val home = System.getProperty("user.home").orEmpty()
    return File(home, ".novabrief/news_cache.db")
}

