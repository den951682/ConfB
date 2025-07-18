package com.force.confbb.feature.terminal

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Terminal(
    id: String,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    Text("Terminal Feature Placeholder FOR ID: $id")
}
