package com.example.novabrief.feature.news.presentation

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.novabrief.BuildConfig
import com.example.novabrief.core.navigation.AppRoute
import com.example.novabrief.feature.news.data.local.SharedNewsDatabaseProvider
import com.example.shared.feature.news.data.remote.NewsApiClient
import com.example.shared.feature.news.data.repository.JvmNewsRepository
import com.example.shared.feature.news.domain.model.NewsArticle
import com.example.shared.feature.news.presentation.NewsDetailScreenUi
import com.example.shared.feature.news.presentation.NewsEmptyState
import com.example.shared.feature.news.presentation.NewsroomLogoMark
import com.example.shared.feature.news.presentation.SharedNewsDetailUiState
import com.example.shared.feature.news.presentation.newsroomLogoFontFamily
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

internal data class NewsDetailUiState(
    val article: NewsArticle? = null,
    val isLoading: Boolean = true
)

class NewsDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {
    private val articleId = savedStateHandle.get<String>(AppRoute.NewsDetail.articleIdArg).orEmpty()
    private val repository = JvmNewsRepository(
        apiService = NewsApiClient.service,
        localDatabase = SharedNewsDatabaseProvider.getInstance(application),
        apiKey = BuildConfig.NEWS_API_KEY
    )

    internal val state: StateFlow<NewsDetailUiState> = repository.observeArticleById(articleId)
        .map { article ->
            NewsDetailUiState(
                article = article,
                isLoading = false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = NewsDetailUiState()
        )
}

@Composable
fun NewsDetailScreen(onBack: () -> Unit) {
    val viewModel: NewsDetailViewModel = viewModel()
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current

    NewsDetailScreenUi(
        uiState = SharedNewsDetailUiState(
            article = uiState.article,
            isLoading = uiState.isLoading
        ),
        onBack = onBack,
        onOpenSource = { url ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        }
    )
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NewsroomLogoMark()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "NEWSROOM",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = newsroomLogoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFF0F2F5)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactActionIcon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                onClick = onBack
            )
            CompactActionIcon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "Save"
            )
            CompactActionIcon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share"
            )
        }
    }
}

@Composable
private fun StoryHeader(article: NewsArticle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetaPill(
                text = article.category.uppercase(),
                background = Color(0xFFF04152),
                contentColor = Color.White
            )
            MetaPill(
                text = article.publishedAt.toDetailTimestamp().uppercase(),
                background = Color(0xFF101722),
                contentColor = Color(0xFF97A2B8)
            )
        }
        Text(
            text = article.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = newsroomLogoFontFamily,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            ),
            color = Color(0xFFF4F6FB)
        )
        if (article.summary.isNotBlank()) {
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                ),
                color = Color(0xFFAFB8CA)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PUBLISHED ${article.publishedAt.toPublishedLabel()}",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF7D8699)
            )
            Text(
                text = article.source.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFFF0B8BE)
            )
        }
    }
}

@Composable
private fun DetailHero(article: NewsArticle) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .background(Color(0xFF0A1220))
    ) {
        if (article.imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color(0xFF050911).copy(alpha = 0.95f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun StoryInsightCard(article: NewsArticle) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .background(Color(0xFF0B1018))
            .border(1.dp, Color(0xFF171E2A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Verified,
                contentDescription = null,
                tint = Color(0xFFE4B84A),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "AT A GLANCE",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFE4B84A)
            )
        }
        Text(
            text = buildInsight(article),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = Color(0xFFC9D2E2)
        )
        HorizontalDivider(color = Color(0xFF171E2A))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InsightStat(label = "Source", value = article.source)
            InsightStat(label = "Topic", value = article.category.replaceFirstChar { it.uppercase() })
        }
    }
}

@Composable
private fun StoryBodyCard(article: NewsArticle) {
    val paragraphs = remember(article.content, article.summary) {
        article.content.ifBlank { article.summary }
            .replace("\\s+".toRegex(), " ")
            .trim()
            .split(". ")
            .map { sentence -> sentence.trim() }
            .filter { it.isNotBlank() }
            .chunked(2)
            .map { chunk ->
                chunk.joinToString(". ").let { paragraph ->
                    if (paragraph.endsWith(".")) paragraph else "$paragraph."
                }
            }
            .ifEmpty {
                listOf("No article body is available for this story yet. Open the original source for the full report.")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(Color(0xFF0B1018))
            .border(1.dp, Color(0xFF171E2A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "FULL ARTICLE",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFF04152)
        )
        paragraphs.forEachIndexed { index, paragraph ->
            Text(
                text = paragraph,
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 29.sp
                ),
                color = Color(0xFFD5DDEA)
            )
            if (index < paragraphs.lastIndex) {
                HorizontalDivider(color = Color(0xFF141B27))
            }
        }
    }
}

@Composable
private fun DetailActionRow(onOpenSource: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFFF04152))
            .clickable(onClick = onOpenSource)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "CONTINUE READING",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        }
        Text(
            text = articleActionHint(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFFBE7EA)
        )
    }
}

@Composable
private fun CommunityStub() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .background(Color(0xFF0B1018))
            .border(1.dp, Color(0xFF171E2A))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "COMMUNITY CHECK",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF8F9AAF)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .background(Color(0xFF0F1621))
                .border(1.dp, Color(0xFF1A2230)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reader notes and fact-check prompts can live here next.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C8699)
            )
        }
    }
}

@Composable
private fun CompactActionIcon(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0A1220))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Color(0xFFA6B0C4),
            modifier = Modifier.size(13.dp)
        )
    }
}

@Composable
private fun MetaPill(
    text: String,
    background: Color,
    contentColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            color = contentColor
        )
    }
}

@Composable
private fun InsightStat(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF738097)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFFE8EEF8)
        )
    }
}

private fun buildInsight(article: NewsArticle): String {
    val subject = article.title.substringBefore(":").ifBlank { article.title }
    return "$subject is the focus here. ${article.summary.ifBlank { "The story is developing and details remain limited." }}"
}

private fun articleActionHint(): String = "Source link"

private fun String.toDetailTimestamp(): String {
    if (isBlank()) return "Just now"

    return try {
        val published = OffsetDateTime.parse(this)
        val duration = Duration.between(published, OffsetDateTime.now())
        when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            else -> published.toLocalDate().toString()
        }
    } catch (_: DateTimeParseException) {
        "Just now"
    }
}

private fun String.toPublishedLabel(): String {
    if (isBlank()) return "UNKNOWN"

    return try {
        OffsetDateTime.parse(this).toLocalDate().toString()
    } catch (_: DateTimeParseException) {
        "UNKNOWN"
    }
}
