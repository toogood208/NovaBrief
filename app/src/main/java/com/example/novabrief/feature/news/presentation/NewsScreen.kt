package com.example.novabrief.feature.news.presentation

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.novabrief.R
import com.example.novabrief.feature.news.data.NewsFeedArticle
import com.example.novabrief.feature.news.data.newsCategories
import com.example.novabrief.feature.news.domain.model.NewsArticle
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import kotlinx.coroutines.delay

internal val newsroomFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

internal val newsroomLogoFontFamily = FontFamily(
    Font(
        googleFont = GoogleFont("Roboto Slab"),
        fontProvider = newsroomFontProvider,
        weight = FontWeight.Bold
    )
)

@Composable
private fun NewsTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NewsroomLogoMark()
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "NEWSROOM",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = newsroomLogoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color(0xFFF0F2F5)
            )
            Text(
                text = "LIVE WIRE",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.6.sp
                ),
                color = Color(0xFF8993A8)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = "Notifications",
                tint = Color(0xFFADB8CD),
                modifier = Modifier.size(24.dp)
            )
            TopBarIconButton(
                imageVector = Icons.AutoMirrored.Outlined.Login,
                contentDescription = "Account"
            )
        }
    }
}

@Composable
fun NewsScreen(
    modifier: Modifier = Modifier,
    onArticleSelected: (String) -> Unit = {},
    onSelectInterests: () -> Unit = {},
    viewModel: NewsViewModel = viewModel()
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val selectedCategory = newsCategories[selectedCategoryIndex]
    val uiState by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(selectedCategoryIndex) {
        listState.scrollToItem(0)
        viewModel.onEvent(NewsUiEvent.OnCategoryChanged(selectedCategory))
    }

    LaunchedEffect(uiState.articles) {
        if (uiState.articles.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    val filteredArticles = uiState.articles.mapIndexed { index, article ->
        article.toNewsFeedArticle(index = index)
    }

    val featured = filteredArticles.firstOrNull()
    val gridArticles = filteredArticles.drop(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF050911))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        NewsTopBar()
        HorizontalDivider(color = Color(0xFF1A2230))
        CategoryTabs(
            selectedIndex = selectedCategoryIndex,
            onCategorySelected = { selectedCategoryIndex = it }
        )
        SectionTitle(title = selectedCategory)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.errorMessage != null && filteredArticles.isNotEmpty()) {
                item(key = "warning-$selectedCategory") {
                    NewsStatusBanner(
                        title = uiState.errorTitle ?: "Refresh interrupted",
                        message = uiState.errorMessage ?: "Showing saved stories for now."
                    )
                }
            }

            if (uiState.isLoading && filteredArticles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFF04152))
                    }
                }
            }

            if (uiState.errorMessage != null && filteredArticles.isEmpty()) {
                item(key = "error-$selectedCategory") {
                    NewsEmptyState(
                        title = uiState.errorTitle ?: "Unable to load articles",
                        message = uiState.errorMessage ?: "We couldn't refresh this section right now.",
                        actionLabel = "TRY AGAIN",
                        onAction = { viewModel.onEvent(NewsUiEvent.OnRefresh) }
                    )
                }
            }

            if (featured != null) {
                item(key = "featured-$selectedCategory") {
                    AnimatedNewsListItem(
                        animationKey = "featured-$selectedCategory",
                        index = 0
                    ) {
                        FeaturedArticleCard(
                            article = featured,
                            onClick = { onArticleSelected(featured.id) }
                        )
                    }
                }
            }

            itemsIndexed(
                items = gridArticles.chunked(2),
                key = { rowIndex, rowItems ->
                    buildString {
                        append(selectedCategory)
                        append('-')
                        append(rowIndex)
                        append('-')
                        append(rowItems.joinToString("-") { it.title })
                    }
                }
            ) { rowIndex, rowItems ->
                AnimatedNewsListItem(
                    animationKey = "$selectedCategory-row-$rowIndex",
                    index = rowIndex + 1
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NewsArticleCard(
                            article = rowItems[0],
                            modifier = Modifier.weight(1f),
                            onClick = { onArticleSelected(rowItems[0].id) }
                        )
                        if (rowItems.size > 1) {
                            NewsArticleCard(
                                article = rowItems[1],
                                modifier = Modifier.weight(1f),
                                onClick = { onArticleSelected(rowItems[1].id) }
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (!uiState.isLoading && !uiState.isInitialLoad && uiState.errorMessage == null && filteredArticles.isEmpty()) {
                item(key = "empty-$selectedCategory") {
                    NewsEmptyState(
                        title = "No articles yet",
                        message = "There are no stories in $selectedCategory right now.",
                        actionLabel = "REFRESH FEED",
                        onAction = { viewModel.onEvent(NewsUiEvent.OnRefresh) }
                    )
                }
            }

            item(key = "footer-$selectedCategory") {
                AnimatedNewsListItem(
                    animationKey = "footer-$selectedCategory",
                    index = gridArticles.chunked(2).size + 1
                ) {
                    FooterBar(onSelectInterests = onSelectInterests)
                }
            }
        }
    }

    // Removed the full screen loading dialog to prevent flickering over cached content
}

private fun NewsArticle.toNewsFeedArticle(index: Int): NewsFeedArticle {
    return NewsFeedArticle(
        id = id,
        category = category.ifBlank { "World" },
        source = source.ifBlank { "Unknown Source" },
        title = title,
        summary = summary.ifBlank { "Tap in for the full story." },
        timeAgo = publishedAt.toNewsTimeAgo(),
        imageUrl = imageUrl,
        imageColor = newsImageColors[index % newsImageColors.size]
    )
}

private fun String.toNewsTimeAgo(): String {
    if (isBlank()) return "Just now"

    return try {
        val published = OffsetDateTime.parse(this)
        val duration = Duration.between(published, OffsetDateTime.now())
        when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            else -> "${duration.toDays()}d ago"
        }
    } catch (_: DateTimeParseException) {
        "Just now"
    }
}

private val newsImageColors = listOf(
    Color(0xFF2C3B4C),
    Color(0xFF89ABC3),
    Color(0xFF1E77A8),
    Color(0xFFA3C6DD),
    Color(0xFF643A8A),
    Color(0xFF567D99)
)

@Composable
private fun NewsArticleImage(
    imageUrl: String,
    imageColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(imageColor),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.onboarding_image),
                placeholder = painterResource(id = R.drawable.onboarding_image)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.onboarding_image),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun AnimatedNewsListItem(
    animationKey: String,
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember(animationKey) { mutableStateOf(false) }

    LaunchedEffect(animationKey) {
        delay((index * 35L).coerceAtMost(180L))
        isVisible = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 850,
            easing = LinearOutSlowInEasing
        ),
        label = "news_item_alpha"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 14.dp,
        animationSpec = tween(
            durationMillis = 850,
            easing = LinearOutSlowInEasing
        ),
        label = "news_item_offset"
    )

    Box(
        modifier = Modifier
            .offset(y = animatedOffsetY)
            .graphicsLayer(alpha = animatedAlpha)
    ) {
        content()
    }
}

@Composable
private fun NewsStatusBanner(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0A1220))
            .border(width = 1.dp, color = Color(0xFF1A2230), shape = RoundedCornerShape(2.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            ),
            color = Color(0xFFF0B8BE)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB2BDCF)
        )
    }
}

@Composable
internal fun NewsEmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = newsroomLogoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFFE9EEF9)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF9AA5BB),
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFF04152))
                .clickable(onClick = onAction)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                tint = Color(0xFFFDF5F5),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFFDF5F5)
            )
        }
    }
}

@Composable
private fun NewsLoadingDialog() {
    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0A1220))
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFFF04152),
                trackColor = Color(0xFF202A38)
            )
            Text(
                text = "Loading stories...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE9EEF9)
            )
        }
    }
}

@Composable
internal fun NewsroomLogoMark() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(Color(0xFFF04152)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "N",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = newsroomLogoFontFamily,
                fontWeight = FontWeight.Bold
            ),
            color = Color.White
        )
    }
}

@Composable
private fun TopBarIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .border(width = 1.dp, color = Color(0xFF202A38), shape = RoundedCornerShape(1.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = Color(0xFFADB8CD),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CategoryTabs(selectedIndex: Int, onCategorySelected: (Int) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(vertical = 10.dp)
    ) {
        itemsIndexed(newsCategories) { index, category ->
            val isSelected = index == selectedIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = category.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color(0xFFE8ECF7) else Color(0xFF7F8AA3),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onCategorySelected(index) }
                        .background(Color.Transparent)
                        .padding(vertical = 2.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .width(72.dp)
                        .height(2.dp)
                        .background(if (isSelected) Color(0xFFF04152) else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(
            text = "SECTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFFF04152)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFFE9EEF9)
        )
    }
}

@Composable
private fun FeaturedArticleCard(
    article: NewsFeedArticle,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(2.dp))
            .clickable(onClick = onClick)
    ) {
        NewsArticleImage(
            imageUrl = article.imageUrl,
            imageColor = article.imageColor,
            contentDescription = article.title,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.85f)
                        ),
                        startY = 0f
                    )
                )
        )

        // Top Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FEATURED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0xFFF04152))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Text(
                    text = article.source.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier
                        .background(Color(0xFF1A1A1A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF121212).copy(alpha = 0.8f))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = newsroomLogoFontFamily,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 26.sp
                ),
                color = Color.White,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0B8C9),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFF8A95AD),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = article.timeAgo.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF8A95AD)
                )
            }
        }
    }
}

@Composable
private fun NewsArticleCard(
    article: NewsFeedArticle,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0A1220))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            NewsArticleImage(
                imageUrl = article.imageUrl,
                imageColor = article.imageColor,
                contentDescription = article.title,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = Color(0xFFE3E9F7),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = article.source.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFF04152),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFF2F6FF),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = article.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9AA5BB),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FooterBar(onSelectInterests: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, start = 14.dp, end = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "@ 2026 NEWSROOM", style = MaterialTheme.typography.labelMedium, color = Color(0xFF848EA4))
        Text(
            text = "SELECT INTERESTS",
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFF0B8BE),
            modifier = Modifier.clickable(onClick = onSelectInterests)
        )
    }
}
