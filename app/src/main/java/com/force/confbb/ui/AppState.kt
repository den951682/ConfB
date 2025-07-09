package com.force.confbb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): AppState {
    return remember(coroutineScope) {
        AppState(coroutineScope)
    }
}

@Stable
class AppState(
    coroutineScope: CoroutineScope
)
