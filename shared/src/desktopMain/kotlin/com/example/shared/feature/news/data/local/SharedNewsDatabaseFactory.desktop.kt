package com.example.shared.feature.news.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.example.shared.feature.news.data.local.db.NovaBriefDatabase
import java.io.File

fun createSharedNewsDatabase(storageFile: File): SharedNewsDatabase {
    storageFile.parentFile?.mkdirs()
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${storageFile.absolutePath}")
    runCatching {
        NovaBriefDatabase.Schema.create(driver)
    }
    return SharedNewsDatabase(driver)
}

