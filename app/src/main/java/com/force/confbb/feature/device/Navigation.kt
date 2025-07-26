package com.force.confbb.feature.device

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
object DeviceSectionRoute

@Serializable
data class DeviceRoute(val id: String, val newDevice: Boolean)

fun NavController.navigateToDeviceSection(navOptions: NavOptions? = null) {
    navigate(DeviceSectionRoute, navOptions)
}

fun NavController.navigateToDevice(id: String, newDevice: Boolean, navOptions: NavOptions? = null) {
    navigate(DeviceRoute(id, newDevice), navOptions)
}

fun NavGraphBuilder.deviceSection(
    onBack: () -> Unit,
    onMessage: suspend (String, Boolean) -> Unit,
    onError: suspend (Throwable?, Boolean) -> Unit,
) {
    navigation<DeviceSectionRoute>(
        startDestination = DeviceRoute("", true),
    ) {
        composable<DeviceRoute> { entry ->
            val id = entry.toRoute<DeviceRoute>().id
            val newDevice = entry.toRoute<DeviceRoute>().newDevice
            Device(
                id,
                onMessage = onMessage,
                onError = onError,
                onBack = onBack,
                viewModel = hiltViewModel<DeviceViewModel, DeviceViewModel.Factory>(
                    key = id
                ) { factory ->
                    factory.create(id, newDevice)
                })
        }
    }
}
