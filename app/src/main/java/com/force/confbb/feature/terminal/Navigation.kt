package com.force.confbb.feature.terminal

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

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
) {
    navigation<TerminalSectionRoute>(
        startDestination = TerminalDevicesRoute,
    ) {
        composable<TerminalDevicesRoute> {
            TerminalDevices(onDeviceClick = onDeviceClick)
        }
        composable<TerminalRoute> { entry ->
            val id = entry.toRoute<TerminalRoute>().id
            Terminal(
                id = id,
                viewModel = hiltViewModel<TerminalViewModel, TerminalViewModel.Factory>(
                    key = id,
                ) { factory ->
                    factory.create(id)
                }
            )
        }
    }
}
