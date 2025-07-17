package com.force.confbb.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun ConfNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = StatusRoute
    ) {
        composable<StatusRoute> {
            Status()
        }
        composable<HistoryRoute> {
            History()
        }
        composable<SettingsRoute> {
            Settings()
        }
    }
}
