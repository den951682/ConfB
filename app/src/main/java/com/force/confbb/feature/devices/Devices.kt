package com.force.confbb.feature.devices

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
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

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Devices(
    modifier: Modifier = Modifier,
    onAddDeviceClick: () -> Unit,
    onDeviceClick: (String) -> Unit,
    onShowSnackbar: suspend (String, String, SnackbarDuration) -> Boolean,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val permissionState = rememberMultiplePermissionsState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                // This permission is normal, and don`t require runtime permission request
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    )
    var requestPermission by remember { mutableStateOf(false) }
    val enabledState by viewModel.isBluetoothEnabled.collectAsStateWithLifecycle()
    val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    val lifecycleOwner = LocalLifecycleOwner.current
    val devices by viewModel.devices.collectAsStateWithLifecycle(
        initialValue = DevicesViewModel.SavedDeviceState.Loading,
        lifecycle = lifecycleOwner.lifecycle
    )
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer//Color(0x50000000)
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic),
                            contentDescription = "App Icon",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.app_name))
                    }
                }
            )
        },
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
                requestPermission && !permissionState.allPermissionsGranted,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
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
            AnimatedVisibility(
                !requestPermission &&
                        devices is DevicesViewModel.SavedDeviceState.Loaded &&
                        (devices as DevicesViewModel.SavedDeviceState.Loaded).devices.isEmpty(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        Icon(
                            painter = painterResource(id = R.drawable.no_devices),
                            contentDescription = null,
                            modifier = Modifier
                                .size(172.dp)
                                .align(Alignment.CenterHorizontally),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(
                            stringResource(R.string.no_devices),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            if (devices is DevicesViewModel.SavedDeviceState.Loaded) {

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items((devices as DevicesViewModel.SavedDeviceState.Loaded).devices) { device ->
                        DeviceCard(
                            device = device,
                            onClick = { onDeviceClick(it.address) },
                            onChangePassphrase = viewModel::onChangePassphrase,
                            onMenuClick = { deviceEntity, action ->
                                when (action) {
                                    "delete" -> viewModel.onDeleteDevice(deviceEntity)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
