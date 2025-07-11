package com.force.confbb.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.force.confbb.R
import com.force.confbb.ui.HistoryRoute
import com.force.confbb.ui.SettingsRoute
import com.force.confbb.ui.StatusRoute
import kotlin.reflect.KClass

enum class TopLevelDestination(
    val icon: ImageVector,
    val textId: Int,
    val route: KClass<*>
) {
    STATUS(Icons.Rounded.Info, R.string.item_status, StatusRoute::class),
    HISTORY(Icons.Rounded.DateRange, R.string.item_history, HistoryRoute::class),
    SETTINGS(Icons.Rounded.Settings, R.string.item_settings, SettingsRoute::class),
}
