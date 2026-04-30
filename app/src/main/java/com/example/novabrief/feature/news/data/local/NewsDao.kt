package com.example.novabrief.feature.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles WHERE category = :category ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM news_articles WHERE id = :id LIMIT 1")
    fun getArticleById(id: String): Flow<NewsArticleEntity?>

    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun getAllArticles(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    @Query("DELETE FROM news_articles WHERE category = :category")
    suspend fun deleteArticlesByCategory(category: String)

    @Query("DELETE FROM news_articles")
    suspend fun clearAllArticles()
}
