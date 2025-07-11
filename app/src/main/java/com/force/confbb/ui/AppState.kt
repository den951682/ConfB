package com.force.confbb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.force.confbb.navigation.TopLevelDestination
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavHostController = rememberNavController()
): AppState {
    return remember(coroutineScope, navController) {
        AppState(navController, coroutineScope)
    }
}

@Stable
class AppState(
    val navController: NavHostController,
    coroutineScope: CoroutineScope
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries
    private val previousDestination = mutableStateOf<NavDestination?>(null)
    val currentDestination: NavDestination?
        @Composable
        get() {
            return navController.currentBackStackEntryAsState().value?.destination?.also {
                previousDestination.value = it
            } ?: previousDestination.value
        }
    val currentTopLevelDestination: TopLevelDestination?
        @Composable
        get() = topLevelDestinations.firstOrNull { currentDestination?.hasRoute(it.route) == true }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val navOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
        when (topLevelDestination) {
            TopLevelDestination.STATUS -> navController.navigateToStatus(navOptions)
            TopLevelDestination.HISTORY -> navController.navigateToHistory(navOptions)
            TopLevelDestination.SETTINGS -> navController.navigateToSettings(navOptions)
        }
    }
}
