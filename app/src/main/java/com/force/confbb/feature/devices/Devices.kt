package com.force.confbb.feature.devices

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.serialization.Serializable

@Serializable
object DevicesRoute

fun NavController.navigateToDevices(navOptions: NavOptions? = null) {
    navigate(DevicesRoute, navOptions)
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Devices(
    modifier: Modifier = Modifier,
    onAddDeviceClick: () -> Unit,
    onShowSnackbar: suspend (String, String, SnackbarDuration) -> Boolean,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_CONNECT
        )
    )
    var requestPermission by remember { mutableStateOf(false) }
    val enabledState by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    stringResource(R.string.enable_bt_hint)
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (permissionState.allPermissionsGranted) {
                        requestPermission = false
                        if (enabledState) {
                            onAddDeviceClick()
                        } else {
                            startForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }
                    } else {
                        requestPermission = true
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { innerPadding ->
        LaunchedEffect(requestPermission) {
            if (requestPermission) {
                permissionState.launchMultiplePermissionRequest()
            }
        }

        val enableBtAction = stringResource(R.string.enable_bt_action)
        val enableBtHint = stringResource(R.string.enable_bt_hint)
        LaunchedEffect(permissionState.allPermissionsGranted, enabledState) {
            if (permissionState.allPermissionsGranted && !enabledState) {
                if (onShowSnackbar(
                        enableBtHint,
                        enableBtAction,
                        SnackbarDuration.Indefinite
                    )
                ) {
                    startForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                requestPermission,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.add_permission_for_bluetooth),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
