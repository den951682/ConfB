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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.confbb.R
import com.force.confbb.designsystem.LoadingWheel
import com.force.confbb.model.DeviceConnectionStatus

@Composable
fun Terminal(
    id: String,
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    val messages by viewModel.items.collectAsStateWithLifecycle(
        lifecycle = lifecycleOwner.lifecycle,
        initialValue = emptyList()
    )
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
        AnimatedVisibility(messages.lastOrNull().run { this is DeviceConnectionStatus.Disconnected || this == null }) {
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
            items(messages) { message ->
                when (message) {
                    is DeviceConnectionStatus.Connected -> {
                        Text(text = stringResource(R.string.connected, message.name))
                    }

                    is DeviceConnectionStatus.Disconnected -> Text(text = stringResource(R.string.connecting))
                    is DeviceConnectionStatus.Error -> {
                        Text(
                            modifier = Modifier
                                .background(Color.Red.copy(alpha = 0.1f))
                                .fillMaxWidth()
                                .padding(4.dp),
                            text = "Error: ${message.trouble?.message ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is DeviceConnectionStatus.Message -> Text(
                        modifier = Modifier
                            .background(Color.Blue.copy(alpha = 0.1f))
                            .fillMaxWidth()
                            .padding(4.dp),
                        text = String(message.byteArray),
                    )

                    is DeviceConnectionStatus.SendMessage -> {
                        Text(
                            modifier = Modifier
                                .background(Color.Green.copy(alpha = 0.1f))
                                .fillMaxWidth()
                                .padding(4.dp),
                            text = String(message.byteArray)
                        )
                    }

                    else -> Unit
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
                enabled = messages.lastOrNull().run {
                    this !is DeviceConnectionStatus.Error
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    viewModel.send(text)
                    text = ""
                },
                enabled = text.run { isNotBlank() } &&
                        messages.lastOrNull().run {
                            this !is DeviceConnectionStatus.Error &&
                                    this !is DeviceConnectionStatus.Disconnected
                        }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}
