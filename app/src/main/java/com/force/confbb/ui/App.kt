package com.force.confbb.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
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
    val icon: ImageVector, val textId: Int
) {
    STATUS(Icons.Rounded.Info, R.string.item_status), HISTORY(Icons.Rounded.DateRange, R.string.item_history), SETTINGS(
        Icons.Rounded.Settings,
        R.string.item_settings
    ),
}

@OptIn(ExperimentalMaterial3Api::class)
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
                item(selected = index == 1, onClick = {}, icon = {
                    Icon(
                        imageVector = item.icon, contentDescription = stringResource(item.textId)
                    )
                }, label = {
                    Text(stringResource(item.textId))
                })
            }
        }, windowAdaptiveInfo = windowAdaptiveInfo
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
            ) {
                val shouldShowTopAppBar = true
                if (shouldShowTopAppBar) {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.item_status)) },
                        actions = {
                            IconButton(
                                onClick = { showDialog = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(
                            if (shouldShowTopAppBar) {
                                WindowInsets(0, 0, 0, 0)
                            } else {
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                            }
                        )
                        .consumeWindowInsets(
                            if (shouldShowTopAppBar) {
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                            } else {
                                WindowInsets(0, 0, 0, 0)
                            }
                        )
                ) {
                    Text("Example Text")
                }
            }
        }
    }
}
