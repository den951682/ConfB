package com.force.confbb.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object StatusRoute

fun NavController.navigateToStatus(navOptions: NavOptions) {
    navigate(StatusRoute, navOptions)
}

@Composable
fun Status(
    modifier: Modifier = Modifier
) {
    Text("Status")
}
