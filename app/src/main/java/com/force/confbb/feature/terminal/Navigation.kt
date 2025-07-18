package com.force.confbb.feature.terminal

import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
object TerminalSectionRoute

@Serializable
object TerminalDevicesRoute

@Serializable
data class TerminalRoute(val id: String)

fun NavController.navigateToTerminalSection(navOptions: NavOptions? = null) {
    navigate(TerminalSectionRoute, navOptions)
}

fun NavController.navigateToTerminalDevices(navOptions: NavOptions? = null) {
    navigate(TerminalDevicesRoute, navOptions)
}

fun NavController.navigateToTerminal(id: String, navOptions: NavOptions? = null) {
    navigate(TerminalRoute(id), navOptions)
}

fun NavGraphBuilder.terminalSection(
    onDeviceClick: (String) -> Unit,
    getBackStackEntry: (KClass<*>) -> NavBackStackEntry
) {
    navigation<TerminalSectionRoute>(
        startDestination = TerminalDevicesRoute,
    ) {
        composable<TerminalDevicesRoute> { entry ->
            val viewModel: TerminalViewModel = hiltViewModel(entry)
            TerminalDevices(
                onDeviceClick = onDeviceClick,
                viewModel = viewModel
            )
        }
        composable<TerminalRoute> { entry ->
            val id = entry.toRoute<TerminalRoute>().id
            val parentEntry = remember(entry) {
                getBackStackEntry(TerminalDevicesRoute::class)
            }
            val viewModel: TerminalViewModel = hiltViewModel(parentEntry)

            Terminal(
                id = id,
                viewModel = viewModel
            )
        }
    }
}
