package com.force.confbb.feature.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.confbb.data.RemoteDevice
import com.force.confbb.designsystem.LoadingWheel

@Composable
fun Device(
    id: String,
    onError: suspend (Throwable?) -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val parameterList = remember { derivedStateOf { viewModel.remoteDevice.parameters.entries.toList() } }
    val state by viewModel.remoteDevice.state.collectAsStateWithLifecycle()

    val error = state is RemoteDevice.State.Error
    val connected = state is RemoteDevice.State.Connected

    LaunchedEffect(error) {
        if (error) {
            kotlin.runCatching { onError((state as? RemoteDevice.State.Error)?.error) }
        }
    }
    if (!error && !connected) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LoadingWheel(
                modifier = Modifier
                    .size(60.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.Center)
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(parameterList.value) { entry ->
            Card {
                Text(entry.value.name ?: "")
            }
        }
    }
}
