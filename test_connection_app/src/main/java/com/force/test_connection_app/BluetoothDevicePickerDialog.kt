package com.force.test_connection_app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
@Composable
fun BluetoothDevicePickerDialog(
    onDismiss: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    val pairedDevices = remember {
        bluetoothAdapter?.bondedDevices?.toList().orEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Вибери пристрій")
        },
        text = {
            if (pairedDevices.isEmpty()) {
                Text("Немає парних пристроїв")
            } else {
                Column {
                    pairedDevices.forEach { device ->
                        Text(
                            text = "${device.name ?: "Без імені"} (${device.address})",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onDeviceSelected(device)
                                    onDismiss()
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрити")
            }
        }
    )
}
