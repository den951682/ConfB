package com.force.confbb.feature.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.confbb.R
import com.force.confbb.designsystem.LoadingWheel
import com.force.confbb.model.ScanDevicesStatus

@Composable
fun TerminalDevices(
    onDeviceClick: (String) -> Unit,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val isBluetoothEnabled by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
        val scanStatus by viewModel.status.collectAsStateWithLifecycle()
        val devices by viewModel.devices.collectAsStateWithLifecycle()

        AnimatedVisibility(
            !isBluetoothEnabled,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.terminal_no_bluetooth),
                    textAlign = TextAlign.Center,
                )
            }
        }
        AnimatedVisibility(
            isBluetoothEnabled,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(R.string.terminal_devices_hint),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    stringResource(R.string.available_devices),
                    style = MaterialTheme.typography.titleLarge
                )
                AnimatedVisibility(scanStatus == ScanDevicesStatus.SCANNING) {
                    LoadingWheel()
                }
                AnimatedVisibility(
                    scanStatus == ScanDevicesStatus.FAILED ||
                            (scanStatus == ScanDevicesStatus.SUCCESS && devices.isEmpty())
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.scan_devices_empty)
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(devices, key = { it.address }) { device ->
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {},
                        ) {
                            Text(text = device.name)
                        }
                    }
                }
                AnimatedVisibility(
                    scanStatus != ScanDevicesStatus.SCANNING,
                ) {
                    Button(
                        onClick = { viewModel.startScan() },
                    ) {
                        Text(stringResource(R.string.retry_search))
                    }
                }
            }
        }
    }
}
