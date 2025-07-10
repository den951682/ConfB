package com.force.confbb.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.force.confbb.R
import com.force.confbb.designsystem.MyNavigationSuiteScaffold

enum class Item(
    val icon: ImageVector,
    val textId: Int
) {
    STATUS(Icons.Rounded.Info, R.string.item_status),
    HISTORY(Icons.Rounded.DateRange, R.string.item_history),
    SETTINGS(Icons.Rounded.Settings, R.string.item_settings),
}

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

    MyNavigationSuiteScaffold(
        items = {
            Item.entries.forEachIndexed { index, item ->
                item(
                    selected = index == 1,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.textId)
                        )
                    },
                    label = {
                        Text(stringResource(item.textId))
                    }
                )
            }
        },
        windowAdaptiveInfo = windowAdaptiveInfo
    ) { }
}
