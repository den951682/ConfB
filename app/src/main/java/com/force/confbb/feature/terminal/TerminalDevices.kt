package com.force.confbb.feature.terminal

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TerminalDevices(
    onDeviceClick: (String) -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    Column {
        Button(
            onClick = { onDeviceClick("A") },
            content = { Text("Go to Terminal A") }
        )
        Button(
            onClick = { onDeviceClick("B") },
            content = { Text("Go to Terminal B") }
        )
    }
}
