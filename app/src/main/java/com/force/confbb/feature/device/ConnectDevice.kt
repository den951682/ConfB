package com.force.confbb.feature.device

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.force.confbb.designsystem.LoadingWheel
import com.force.confbb.model.DeviceConnectionStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectDevice(
    id: String,
    onConnected: (String) -> Unit,
    onError: suspend (Throwable?) -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsStateWithLifecycle()
    val error by remember { derivedStateOf { message is DeviceConnectionStatus.Error } }
    val connected by remember { derivedStateOf { message is DeviceConnectionStatus.DataMessage } }
    if (connected) {
        onConnected(id)
    }
    LaunchedEffect(error) {
        if (error) {
            onError((message as DeviceConnectionStatus.Error).trouble)
        }
    }
    if (!error) {
        BasicAlertDialog(
            onDismissRequest = { },
        ) {
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
    }
}
