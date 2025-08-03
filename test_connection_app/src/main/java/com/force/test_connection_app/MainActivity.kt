package com.force.test_connection_app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.connection.connection.DeviceConnection
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class, ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val permissionState = rememberMultiplePermissionsState(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    listOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_ADVERTISE
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
            val startForResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
            val connection by viewModel.connection.collectAsStateWithLifecycle(null)
            val connectionInfo by viewModel.connection.flatMapLatest { it.info }.collectAsStateWithLifecycle(null)
            val connectionState by viewModel.connection.flatMapLatest { it.state }.collectAsStateWithLifecycle(
                DeviceConnection.State.Connecting
            )
            val connectionData by viewModel.connection.flatMapLatest { it.dataObjects }.collectAsStateWithLifecycle(
                ""
            )
            MaterialTheme {
                LaunchedEffect(requestPermission) {
                    if (requestPermission) {
                        permissionState.launchMultiplePermissionRequest()
                        requestPermission = false
                    }
                }
                Scaffold(
                    topBar = {
                        val name = connectionInfo?.name ?: ""
                        val address = connectionInfo?.address ?: ""
                        TopAppBar(
                            title = { Text("$name  $address") }
                        )
                    }
                ) { paddingValues ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        if (connection == null) {
                            Button(
                                onClick = { viewModel.startWifiServer() }
                            ) {
                                Text("Wifi Server")
                            }
                            Button(
                                onClick = { viewModel.startWifiClient() }
                            ) {
                                Text("Wifi Client ")
                            }
                            Button(
                                onClick = {
                                    if (permissionState.allPermissionsGranted) {
                                        requestPermission = false
                                        if (isBluetoothAvailableAndEnabled()) {
                                            viewModel.startBtServer()
                                        } else {
                                            startForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                        }
                                    } else {
                                        requestPermission = true
                                    }
                                }
                            ) {
                                Text("Bluetooth Server")
                            }
                            Button(
                                onClick = {
                                    if (permissionState.allPermissionsGranted) {
                                        requestPermission = false
                                        if (isBluetoothAvailableAndEnabled()) {
                                            viewModel.startBtClient()
                                        } else {
                                            startForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                                        }
                                    } else {
                                        requestPermission = true
                                    }
                                }
                            ) {
                                Text("Bluetooth Client ")
                            }
                        } else {
                            val stateText = if (connectionState is DeviceConnection.State.Error) {
                                "ERROR: " + (connectionState as DeviceConnection.State.Error).error.message
                            } else {
                                "${connectionState::class.simpleName}"
                            }
                            Text(
                                text = stateText,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            val data = connectionData.toString()
                            Text(
                                text = data,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }

    fun isBluetoothAvailableAndEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled
    }

}
