package com.example.novabrief.feature.news.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TopHeadlinesResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("articles") val articles: List<ArticleDto>
)

data class ArticleDto(
    @SerializedName("source") val source: SourceDto?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("content") val content: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("urlToImage") val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?
)

data class SourceDto(
    @SerializedName("name") val name: String?
)
