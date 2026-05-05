package com.example.shared.feature.news.presentation

import kotlin.js.Date

actual fun formatNewsTimeAgo(publishedAt: String): String {
    if (publishedAt.isBlank()) return "Just now"

    val publishedMs = Date.parse(publishedAt)
    if (publishedMs.isNaN()) return "Just now"

    val nowMs = Date.now()
    val elapsedMs = nowMs - publishedMs
    if (elapsedMs <= 0.0) return "Just now"

    val minutes = (elapsedMs / (1000.0 * 60.0)).toInt()
    val hours = (elapsedMs / (1000.0 * 60.0 * 60.0)).toInt()
    val days = (elapsedMs / (1000.0 * 60.0 * 60.0 * 24.0)).toInt()

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}

