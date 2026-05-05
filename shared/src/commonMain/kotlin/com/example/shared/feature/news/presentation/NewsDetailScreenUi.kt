package com.example.shared.feature.news.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shared.feature.news.domain.model.NewsArticle

data class SharedNewsDetailUiState(
    val article: NewsArticle? = null,
    val isLoading: Boolean = true
)

@Composable
fun NewsDetailScreenUi(
    uiState: SharedNewsDetailUiState,
    onBack: () -> Unit,
    onOpenSource: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050911))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        DetailTopBar(onBack = onBack)
        HorizontalDivider(color = Color(0xFF1A2230))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFF04152))
                }
            }

            uiState.article == null -> {
                NewsEmptyState(
                    title = "Story unavailable",
                    message = "This article is no longer in local cache. Refresh the feed and try again.",
                    actionLabel = "GO BACK",
                    onAction = onBack
                )
            }

            else -> {
                val article = uiState.article
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Text(
                            text = article.category.ifBlank { "World" }.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFF04152),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                    item {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = newsroomLogoFontFamily,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 34.sp
                            ),
                            color = Color(0xFFF4F6FB),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = article.source.ifBlank { "Unknown Source" }.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFF0B8BE)
                            )
                            Text(
                                text = article.publishedAt.toDetailHeaderTime().uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8A95AD)
                            )
                        }
                    }
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .padding(horizontal = 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFF0A1220))
                        ) {
                            PlatformArticleImage(
                                imageUrl = article.imageUrl,
                                contentDescription = article.title,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.15f),
                                                Color(0xFF050911).copy(alpha = 0.9f)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                    item {
                        Text(
                            text = article.summary.ifBlank { "No summary available." },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFAFB8CA),
                            modifier = Modifier.padding(horizontal = 14.dp),
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    item {
                        Text(
                            text = article.content.ifBlank { article.summary },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD5DDEA),
                            modifier = Modifier.padding(horizontal = 14.dp),
                            lineHeight = 24.sp
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFF04152))
                                .clickable { onOpenSource(article.articleUrl) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    }
                }
            }
        }
    }
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
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF0A1220))
                .clickable(onClick = onBack)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFFA6B0C4),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "BACK",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFA6B0C4)
            )
        }
    }
}

private fun String.toDetailHeaderTime(): String {
    if (isBlank()) return "Just now"
    val datePart = substringBefore('T', missingDelimiterValue = this)
    return if (datePart.length >= 10) datePart else formatNewsTimeAgo(this)
}

