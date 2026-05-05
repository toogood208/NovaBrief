package com.example.shared.feature.news.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.shared.feature.news.data.local.db.NovaBriefDatabase

fun createSharedNewsDatabase(context: Context): SharedNewsDatabase {
    val driver = AndroidSqliteDriver(
        schema = NovaBriefDatabase.Schema,
        context = context.applicationContext,
        name = "novabrief_cache.db"
    )
    return SharedNewsDatabase(driver)
}

