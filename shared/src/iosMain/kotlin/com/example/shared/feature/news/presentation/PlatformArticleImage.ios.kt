package com.example.shared.feature.news.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformArticleImage(
    imageUrl: String,
    contentDescription: String,
    modifier: Modifier
) {
    // iOS fallback keeps the color placeholder from NewsArticleImage.
}

