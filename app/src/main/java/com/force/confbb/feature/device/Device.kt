package com.force.confbb.feature.device

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Device(
    id: String,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    Text(text = "Device: $id")
}
