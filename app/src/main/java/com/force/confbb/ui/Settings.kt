package com.force.confbb.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

fun NavController.navigateToSettings(navOptions: NavOptions) {
    navigate(SettingsRoute, navOptions)
}

@Composable
fun Settings(
    modifier: Modifier = Modifier
) {
    Text("Settings")
}
