package com.force.confbb.feature.scan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.force.confbb.R
import com.force.confbb.designsystem.LoadingWheel
import com.force.confbb.model.ScanDevicesStatus
import kotlinx.serialization.Serializable

@Serializable
object ScanRoute

fun NavController.navigateToScan(navOptions: NavOptions? = null) {
    navigate(ScanRoute, navOptions)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDevices(
    onDismiss: () -> Unit = {},
    viewModel: ScanDevicesViewModel = hiltViewModel()
) {
    val scanStatus by viewModel.status.collectAsStateWithLifecycle()
    val devices by viewModel.devices.collectAsStateWithLifecycle()

    BasicAlertDialog(
        onDismissRequest = { },
    ) {
        Surface(
            modifier = Modifier
                .widthIn(364.dp)
                .heightIn(400.dp, 400.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(scanStatus == ScanDevicesStatus.SCANNING) {
                        LoadingWheel(
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(stringResource(R.string.scan_devices_title), style = MaterialTheme.typography.titleLarge)
                }
                AnimatedVisibility(
                    scanStatus == ScanDevicesStatus.FAILED,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        textAlign = TextAlign.Center,
                        text = stringResource(R.string.scan_devices_failed)
                    )
                }
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(devices, key = { it.name }) { device ->
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
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Button(
                        onClick = { viewModel.startScan() },

                        ) {
                        Text(stringResource(R.string.retry))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
