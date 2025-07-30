package com.force.confbb.feature.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.confbb.R
import com.force.confbb.analytics.AnalyticsLogger
import com.force.confbb.data.device.DeviceConnection
import com.force.confbb.designsystem.LoadingWheel

@Composable
fun Terminal(
    id: String,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    AnalyticsLogger.logScreenView("terminal_screen")
    val state by viewModel.state.collectAsState()
    val messages by viewModel.items.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val listState = rememberLazyListState()
        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }
        AnimatedVisibility(state == DeviceConnection.State.Connecting) {
            LoadingWheel(modifier = Modifier.size(32.dp))
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if(state is DeviceConnection.State.Connected)  {
                item{
                    Text(text = stringResource(R.string.connected))
                }
            }
            items(messages) { (incoming, message) ->
                if (incoming) {
                    Text(
                        modifier = Modifier
                            .background(Color.Blue.copy(alpha = 0.1f))
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = message,
                    )
                } else {
                    Text(
                        modifier = Modifier
                            .background(Color.Green.copy(alpha = 0.1f))
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = message
                    )
                }
            }
            item {
                when (state) {
                    is DeviceConnection.State.Connecting -> {
                        Text(text = stringResource(R.string.connecting))
                    }

                    is DeviceConnection.State.Disconnected -> {
                        Text(text = stringResource(R.string.disconnected))
                    }

                    is DeviceConnection.State.Error -> {
                        val error = state as DeviceConnection.State.Error
                        Text(
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.1f))
                                .fillMaxWidth()
                                .padding(4.dp),
                            text = "Error: ${error.error.message ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    DeviceConnection.State.Connected -> Unit
                }
            }
        }
        var text by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .weight(1f),

                placeholder = { Text(stringResource(R.string.text_to_send)) },
                enabled = state !is DeviceConnection.State.Error
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.send(text)
                    text = ""
                },
                enabled = text.run { isNotBlank() } && state is DeviceConnection.State.Connected,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}
