package com.force.confbb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.force.confbb.feature.devices.navigateToDevices
import com.force.confbb.feature.terminal.navigateToTerminalSection
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
        @Composable get() {
            val currentEntry = navController.currentBackStackEntryFlow
                .collectAsState(initial = null)

            return currentEntry.value?.destination.also { destination ->
                if (destination != null) {
                    previousDestination.value = destination
                }
            } ?: previousDestination.value
        }

    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            return TopLevelDestination.entries.firstOrNull { topLevelDestination ->
                currentDestination?.hasRoute(route = topLevelDestination.route) == true
            }
        }

    fun navigateToTopLevelDestination(topLevelDestination: TopLevelDestination) {
        val navOptions = navOptions {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
                inclusive = false
            }
            launchSingleTop = true
            restoreState = false
        }
        when (topLevelDestination) {
            TopLevelDestination.DEVICES -> navController.navigateToDevices(navOptions)
            TopLevelDestination.TERMINAL -> navController.navigateToTerminalSection(navOptions)
            TopLevelDestination.SETTINGS -> navController.navigateToSettings(navOptions)
        }
    }
}
