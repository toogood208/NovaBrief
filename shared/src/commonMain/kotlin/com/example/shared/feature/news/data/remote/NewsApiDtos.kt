package com.example.shared.feature.news.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopHeadlinesResponseDto(
    @SerialName("status") val status: String,
    @SerialName("totalResults") val totalResults: Int,
    @SerialName("articles") val articles: List<ArticleDto>
)

@Serializable
data class ArticleDto(
    @SerialName("source") val source: SourceDto?,
    @SerialName("title") val title: String?,
    @SerialName("description") val description: String?,
    @SerialName("content") val content: String?,
    @SerialName("url") val url: String?,
    @SerialName("urlToImage") val urlToImage: String?,
    @SerialName("publishedAt") val publishedAt: String?
)

@Serializable
data class SourceDto(
    @SerialName("name") val name: String?
)

