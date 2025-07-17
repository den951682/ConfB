package com.force.confbb.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.force.confbb.feature.devices.Devices
import com.force.confbb.feature.devices.DevicesRoute
import com.force.confbb.feature.scan.ScanDevices
import com.force.confbb.feature.scan.ScanRoute
import com.force.confbb.feature.scan.navigateToScan

@Composable
fun ConfNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = DevicesRoute
    ) {
        composable<DevicesRoute> {
            Devices(
                onAddDeviceClick = navController::navigateToScan,
            )
        }
        composable<HistoryRoute> {
            History()
        }
        composable<SettingsRoute> {
            Settings()
        }
        dialog<ScanRoute> {
            ScanDevices(
                onDismiss = navController::popBackStack
            )
        }
    }
}
