package com.example.shared.feature.news.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEverything(
        @Query("apiKey") apiKey: String,
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("searchIn") searchIn: String = "title,description",
        @Query("excludeDomains") excludeDomains: String? = null,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 20
    ): TopHeadlinesResponseDto

    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String,
        @Query("country") country: String = "us",
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int = 20
    ): TopHeadlinesResponseDto
}

