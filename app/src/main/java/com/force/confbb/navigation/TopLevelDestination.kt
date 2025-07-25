package com.force.confbb.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.force.confbb.R
import com.force.confbb.feature.devices.DevicesRoute
import com.force.confbb.feature.terminal.TerminalDevicesRoute
import com.force.confbb.feature.terminal.TerminalSectionRoute
import com.force.confbb.ui.InfoRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val iconRes: Int?,
    val icon: ImageVector?,
    val textId: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route
) {
    DEVICES(null, Icons.Rounded.Home, R.string.item_devices, DevicesRoute::class),
    TERMINAL(
        R.drawable.terminal,
        null,
        R.string.item_terminal,
        TerminalSectionRoute::class,
        TerminalDevicesRoute::class
    ),
    INFO(null, Icons.Rounded.Info, R.string.item_info, InfoRoute::class),
}
