package com.force.confbb.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
object HistoryRoute

fun NavController.navigateToHistory(navOptions: NavOptions) {
    navigate(HistoryRoute, navOptions)
}

@Composable
fun History(
    modifier: Modifier = Modifier
) {
    Text("History")
}
