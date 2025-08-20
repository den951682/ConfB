import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.force.confbb.R
import com.force.model.Device

@Composable
fun ProtocolDialog(
    selected: Device.Protocol,
    onDismiss: () -> Unit,
    onConfirm: (Device.Protocol) -> Unit
) {
    var current by remember { mutableStateOf(selected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(current) }) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        title = { Text(stringResource(R.string.change_protocol)) },
        text = {
            Column {
                Device.Protocol.entries.forEach { protocol ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { current = protocol }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = (current == protocol),
                            onClick = { current = protocol }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = protocol.asString()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun Device.Protocol.asString() = when (this) {
    Device.Protocol.EPHEMERAL ->
        stringResource(R.string.protocol_ephemeral)

    Device.Protocol.PASSPHRASE ->
        stringResource(R.string.protocol_passphrase)

    Device.Protocol.RAW ->
        stringResource(R.string.protocol_raw)
}
