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
data class DeviceRoute(val id: String)

fun NavController.navigateToDeviceSection(navOptions: NavOptions? = null) {
    navigate(DeviceSectionRoute, navOptions)
}

fun NavController.navigateToDevice(id: String, navOptions: NavOptions? = null) {
    navigate(DeviceRoute(id), navOptions)
}

fun NavGraphBuilder.deviceSection(
    onError: suspend (Throwable?) -> Unit,
) {
    navigation<DeviceSectionRoute>(
        startDestination = DeviceRoute(""),
    ) {
        composable<DeviceRoute> { entry ->
            val id = entry.toRoute<DeviceRoute>().id
            Device(
                id,
                onError = onError,
                viewModel = hiltViewModel<DeviceViewModel, DeviceViewModel.Factory>(
                    key = id
                ) { factory ->
                    factory.create(id)
                })
        }
    }
}
