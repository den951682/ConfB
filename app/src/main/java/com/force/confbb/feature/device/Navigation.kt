package com.force.confbb.feature.device

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
object DeviceSectionRoute

@Serializable
data class ConnectDeviceRoute(val id: String)

@Serializable
data class DeviceRoute(val id: String)

fun NavController.navigateToDeviceSection(navOptions: NavOptions? = null) {
    navigate(DeviceSectionRoute, navOptions)
}

fun NavController.navigateToConnectDevice(id: String, navOptions: NavOptions? = null) {
    navigate(ConnectDeviceRoute(id), navOptions)
}

fun NavController.navigateToDevice(id: String, navOptions: NavOptions? = null) {
    navigate(DeviceRoute(id), navOptions)
}

fun NavGraphBuilder.deviceSection(
    parentEntry: (KClass<*>) -> NavBackStackEntry,
    onConnected: (String) -> Unit,
    onError: suspend (Throwable?) -> Unit,
) {
    navigation<DeviceSectionRoute>(
        startDestination = ConnectDeviceRoute(""),
    ) {
        composable<ConnectDeviceRoute> { entry ->
            val id = entry.toRoute<ConnectDeviceRoute>().id
            ConnectDevice(
                id,
                onConnected = onConnected,
                onError = onError,
                viewModel = hiltViewModel<DeviceViewModel, DeviceViewModel.Factory>(
                    viewModelStoreOwner = entry,
                    key = id,
                ) { factory ->
                    factory.create(id)
                })
        }
        composable<DeviceRoute> { entry ->
            val id = entry.toRoute<DeviceRoute>().id
            Device(
                id, viewModel = hiltViewModel<DeviceViewModel, DeviceViewModel.Factory>(
                    viewModelStoreOwner = entry,//parentEntry(DeviceRoute::class),
                    key = id,
                ) { factory ->
                    factory.create(id)
                })
        }
    }
}
