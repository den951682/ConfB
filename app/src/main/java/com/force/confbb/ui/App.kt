package com.force.confbb.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.force.confbb.designsystem.MyNavigationSuiteScaffold
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    appState: AppState,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()
) {
    val currentDestination = appState.currentDestination
    val snackbarHostState = remember { SnackbarHostState() }
    val destination = appState.currentTopLevelDestination
    MyNavigationSuiteScaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
        items = {
            appState.topLevelDestinations.forEach { item ->
                item(
                    selected = currentDestination?.isRouteInHierarchy(item.route) == true,
                    onClick = {
                        if (destination != item) {
                            appState.navigateToTopLevelDestination(item)
                        }
                    },
                    icon = {
                        val icon = item.icon ?: item.iconRes?.let { ImageVector.vectorResource(id = it) }
                        Icon(
                            imageVector = icon!!, contentDescription = stringResource(item.textId),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(stringResource(item.textId))
                    }
                )
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
                Box(
                    modifier = Modifier
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        )
                        .consumeWindowInsets(
                            WindowInsets(0, 0, 0, 0)
                        )
                ) {
                    ConfNavHost(
                        appState.navController,
                        onShowSnackbar = { message, action, length ->
                            snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = action,
                                duration = length,
                            ) == ActionPerformed
                        }
                    )
                }
            }
        }
    }
}

private fun NavDestination?.isRouteInHierarchy(route: KClass<*>) =
    this?.hierarchy?.any {
        it.hasRoute(route)
    } ?: false
