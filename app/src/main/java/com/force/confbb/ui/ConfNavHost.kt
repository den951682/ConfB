package com.force.confbb.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.force.confbb.feature.device.deviceSection
import com.force.confbb.feature.device.navigateToDevice
import com.force.confbb.feature.devices.Devices
import com.force.confbb.feature.devices.DevicesRoute
import com.force.confbb.feature.scan.ScanDevices
import com.force.confbb.feature.scan.ScanRoute
import com.force.confbb.feature.scan.navigateToScan
import com.force.confbb.feature.terminal.navigateToTerminal
import com.force.confbb.feature.terminal.terminalSection

@Composable
fun ConfNavHost(
    navController: NavHostController,
    onShowSnackbar: suspend (String, String?, SnackbarDuration) -> Boolean
) {
    NavHost(
        navController = navController,
        startDestination = DevicesRoute
    ) {
        composable<DevicesRoute> {
            Devices(
                onShowSnackbar = onShowSnackbar,
                onAddDeviceClick = navController::navigateToScan,
            )
        }
        deviceSection(
            onBack = navController::popBackStack,
            onError = {
                onShowSnackbar(
                    it?.message ?: "Щось не так",
                    null,
                    SnackbarDuration.Indefinite
                )
            },
        )
        terminalSection(
            onDeviceClick = navController::navigateToTerminal
        )
        composable<SettingsRoute> {
            Settings()
        }
        dialog<ScanRoute> {
            ScanDevices(
                onDismiss = navController::popBackStack,
                onDeviceClick = { id ->
                    navController.popBackStack()
                    navController.navigateToDevice(id)
                },
            )
        }
    }
}
