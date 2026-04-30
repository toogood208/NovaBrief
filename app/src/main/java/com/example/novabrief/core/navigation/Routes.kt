package com.example.novabrief.core.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    data object News : AppRoute("news")
    data object NewsDetail : AppRoute("news_detail/{articleId}") {
        const val articleIdArg = "articleId"

        fun createRoute(articleId: String): String {
            return "news_detail/${Uri.encode(articleId)}"
        }
    }
    data object Personalization : AppRoute("personalization")
}
