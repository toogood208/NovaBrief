package com.example.novabrief.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novabrief.feature.news.presentation.NewsDetailScreen
import com.example.novabrief.feature.news.presentation.NewsScreen
import com.example.novabrief.feature.personalization.presentation.PersonalizationScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoute.News.route
    ) {
        composable(AppRoute.News.route) {
            NewsScreen(
                onArticleSelected = { articleId ->
                    navController.navigate(AppRoute.NewsDetail.createRoute(articleId))
                },
                onSelectInterests = {
                    navController.navigate(AppRoute.Personalization.route)
                }
            )
        }
        composable(
            route = AppRoute.NewsDetail.route,
            arguments = listOf(
                navArgument(AppRoute.NewsDetail.articleIdArg) {
                    type = NavType.StringType
                }
            )
        ) {
            NewsDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoute.Personalization.route) {
            PersonalizationScreen(
                onBack = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }
    }
}
