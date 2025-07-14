package com.force.confbb.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.force.confbb.designsystem.DraggableScrollbar
import com.force.confbb.designsystem.ScrollbarState
import com.force.confbb.designsystem.rememberDraggableScroller
import com.force.confbb.designsystem.scrollbarState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
object SettingsRoute

fun NavController.navigateToSettings(navOptions: NavOptions) {
    navigate(SettingsRoute, navOptions)
}

data class I(val id: String, val text: String, val value: Int)

@Composable
fun Settings(
    modifier: Modifier = Modifier
) {
    val list = (1..40).map { I(it.toString(), "$it", it) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val staggeredState = rememberLazyStaggeredGridState()
        val itemsAvailable = list.size + 1
        val scroller = staggeredState.rememberDraggableScroller(
            itemsAvailable = itemsAvailable
        )
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(120.dp),
            contentPadding = PaddingValues(16.dp),
            state = staggeredState,
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(
                                constraints.copy(
                                    minWidth = constraints.maxWidth + 32.dp.roundToPx(),
                                    maxWidth = constraints.maxWidth + 32.dp.roundToPx(),
                                ),
                            )
                            layout(placeable.width, placeable.height) {
                                placeable.place(0, 0)
                            }
                        }
                        .background(Color.Cyan)
                )
            }
            items(list, key = { it.id }) { item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(max(30.dp, (item.value * 7).dp))
                        .background(Color.Gray)
                        .then(
                            if (item.value == 1) {
                                Modifier
                                    .background(Color.Red)
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(
                                            constraints.copy(
                                                maxWidth = constraints.maxWidth + 32.dp.roundToPx(),
                                            )
                                        )
                                        layout(placeable.width, placeable.height) {
                                            placeable.place(0, 0)
                                        }
                                    }
                            } else {
                                Modifier
                            }
                        )

                )
                {
                    Text(
                        text = item.text,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        val visible = flow<Boolean> {
            delay(1000)
            emit(false)
        }.collectAsStateWithLifecycle(
            initialValue = true
        )
        TextButton(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.Blue),
            onClick = {
                scroller(0.5f + Random.nextFloat() * 0.001f) // Прокрутка к середине списка
            }
        ) {
            Text("Прокрутить к середине")
        }
        staggeredState.DraggableScrollbar(
            Modifier
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 2.dp)
                .align(Alignment.CenterEnd),
            Orientation.Vertical,
            staggeredState.scrollbarState(itemsAvailable),
            scroller
        )

        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(),
            visible = visible.value,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 700) // длительность появления
            ) + fadeIn(animationSpec = tween(700)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 500) // длительность исчезновения
            ) + fadeOut(animationSpec = tween(500)),
        ) {
            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .size(60.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Yellow)
            ) {
            }

        }
    }
}
