package com.example.shared.feature.news.presentation

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

actual fun formatNewsTimeAgo(publishedAt: String): String {
    if (publishedAt.isBlank()) return "Just now"

    return try {
        val published = OffsetDateTime.parse(publishedAt)
        val duration = Duration.between(published, OffsetDateTime.now())

        when {
            duration.isNegative || duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            else -> "${duration.toDays()}d ago"
        }
    } catch (_: DateTimeParseException) {
        "Just now"
    }
}

