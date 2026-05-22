package com.nextersolutions.soulmetric.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nextersolutions.soulmetric.feature.home.presentation.screen.HomeScreen
import com.nextersolutions.soulmetric.feature.results.presentation.screen.ResultsScreen
import com.nextersolutions.soulmetric.feature.survey.presentation.screen.SurveyScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Survey : Screen("survey/{surveyId}") {
        fun createRoute(surveyId: String) = "survey/$surveyId"
    }

    data object Results : Screen("results")
}

@Composable
fun SoulMetricNavHost(navController: NavHostController) {
    NavHost(
        modifier = Modifier.fillMaxSize(),
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSurvey = { id ->
                    navController.navigate(Screen.Survey.createRoute(id))
                },
                onNavigateToResults = { navController.navigate(Screen.Results.route) }
            )
        }

        composable(
            route = Screen.Survey.route,
            arguments = listOf(navArgument("surveyId") { type = NavType.StringType })
        ) {
            SurveyScreen(
                onNavigateBack = { navController.popBackStack() },
                onSurveyCompleted = {
                    navController.popBackStack()
                    navController.navigate(Screen.Results.route)
                }
            )
        }

        composable(Screen.Results.route) {
            ResultsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
