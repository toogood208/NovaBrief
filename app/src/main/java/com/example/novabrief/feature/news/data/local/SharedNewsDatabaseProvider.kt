package com.example.novabrief.feature.news.data.local

import android.content.Context
import com.example.shared.feature.news.data.local.SharedNewsDatabase
import com.example.shared.feature.news.data.local.createSharedNewsDatabase

object SharedNewsDatabaseProvider {
    @Volatile
    private var instance: SharedNewsDatabase? = null

    fun getInstance(context: Context): SharedNewsDatabase {
        return instance ?: synchronized(this) {
            instance ?: createSharedNewsDatabase(context).also { created ->
                instance = created
            }
        }
    }
}

