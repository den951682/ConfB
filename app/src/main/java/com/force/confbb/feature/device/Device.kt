package com.force.confbb.feature.device

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.force.confb.pmodel.Message
import com.force.confbb.data.RemoteDevice
import com.force.confbb.designsystem.LoadingWheel
import com.force.confbb.designsystem.NumValueSelector
import com.force.confbb.model.ConfError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Device(
    id: String,
    onMessage: suspend (String, Boolean) -> Unit,
    onError: suspend (Throwable?, Boolean) -> Unit,
    onBack: () -> Unit,
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val parameterList = remember { derivedStateOf { viewModel.remoteDevice.parameters.entries.toList() } }
    val state by viewModel.remoteDevice.state.collectAsStateWithLifecycle()

    val error = state is RemoteDevice.State.Error
    val connected = state is RemoteDevice.State.Connected

    LaunchedEffect(Unit) {
        viewModel.remoteDevice.events.collect { event ->
            when (event.obj) {
                is Message -> {
                    onMessage((event.obj as Message).text.toStringUtf8(), false)
                }
            }
        }
    }
    LaunchedEffect(error) {
        if (error) {
            val e = (state as? RemoteDevice.State.Error)?.error
            runCatching { onError(e, (e as? ConfError)?.isCritical == true) }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = viewModel.remoteDevice.name, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            actions = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Роз'єднати")
                }
            }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(parameterList.value) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = if (entry.value.editable) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    } else {
                        CardDefaults.cardColors(containerColor = Color.Transparent)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.value.name ?: "", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(entry.value.description ?: "", style = MaterialTheme.typography.bodyMedium)
                            if (entry.value.editable && entry.value.value is String) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = entry.value.value.toString().take(15),
                                        onValueChange = { newText ->
                                            val trimmed = newText.take(15)
                                            viewModel.onValueChanged(entry.key, trimmed)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .padding(vertical = 0.dp),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodyLarge,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .wrapContentSize()
                                            .align(Alignment.CenterStart)
                                    ) {
                                        this@Row.AnimatedVisibility(entry.value.changeRequestSend) {
                                            LoadingWheel(modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                            }
                        }
                        if (entry.value.editable) {
                            if (entry.value.value !is String) {
                                Box(modifier = Modifier.wrapContentSize()) {
                                    Spacer(modifier = Modifier.size(32.dp))
                                    this@Row.AnimatedVisibility(entry.value.changeRequestSend) {
                                        LoadingWheel(modifier = Modifier.size(32.dp))
                                    }
                                }
                                if (entry.value.value is Int) {
                                    NumValueSelector(
                                        value = entry.value.value as Int,
                                        onValueChange = { viewModel.onValueChanged(entry.key, it) },
                                        modifier = Modifier.wrapContentSize(),
                                        range =
                                            ((entry.value.minValue as? Int)?.toFloat() ?: Float.NEGATIVE_INFINITY)..
                                                    ((entry.value.maxValue as? Int)?.toFloat()
                                                        ?: Float.POSITIVE_INFINITY)
                                    )
                                }
                                if (entry.value.value is Float) {
                                    NumValueSelector(
                                        value = entry.value.value as Float,
                                        onValueChange = { viewModel.onValueChanged(entry.key, it) },
                                        modifier = Modifier.wrapContentSize(),
                                        step = 0.05,
                                        decimalPlaces = 2,
                                        range =
                                            (entry.value.minValue as? Float ?: Float.NEGATIVE_INFINITY)..
                                                    ((entry.value.maxValue as? Float) ?: Float.POSITIVE_INFINITY)
                                    )
                                }
                                if (entry.value.value is Boolean) {
                                    Switch(
                                        checked = entry.value.value as? Boolean == true,
                                        onCheckedChange = { viewModel.onValueChanged(entry.key, it) }
                                    )
                                }
                            }
                        } else {
                            Text(
                                modifier = Modifier.padding(start = 8.dp),
                                text = entry.value.value.formatValue(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    if (!error && !connected) {
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

@SuppressLint("DefaultLocale")
private fun Any?.formatValue(): String {
    return when (this) {
        is Int -> toString()
        is Float -> String.format("%.2f", this)
        is String -> this
        is Boolean -> if (this) "Увімкнено" else "Вимкнено"
        else -> this?.toString() ?: "N/A"
    }
}
