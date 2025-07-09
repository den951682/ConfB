package com.force.confbb.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.force.confbb.R

@Composable
fun App(
    appState: AppState,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    if (showDialog) {
        SettingsDialog { showDialog = false }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val showMessage = true
    val stringMessage = stringResource(R.string.test_message)
    LaunchedEffect(showMessage) {
        if (showMessage) {
            snackbarHostState.showSnackbar(message = stringMessage, duration = SnackbarDuration.Indefinite)
        }
    }

    TextButton(
        onClick = { showDialog = true },
    ) {
        Text("Settings")
    }
}
