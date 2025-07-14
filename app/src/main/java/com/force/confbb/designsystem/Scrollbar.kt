package com.force.confbb.designsystem

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.Orientation.Horizontal
import androidx.compose.foundation.gestures.Orientation.Vertical
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeout
import java.lang.Float.max
import kotlin.math.min
import kotlin.math.roundToInt

internal fun Orientation.valueOf(offset: Offset) = when (this) {
    Vertical -> offset.y
    Horizontal -> offset.x
}

internal fun Orientation.valueOf(offset: IntOffset) = when (this) {
    Vertical -> offset.y
    Horizontal -> offset.x
}

internal fun Orientation.valueOf(intSize: IntSize) = when (this) {
    Vertical -> intSize.height
    Horizontal -> intSize.width
}

@Immutable
@JvmInline
private value class ScrollbarTrack(val packedValue: Long) {
    constructor(max: Float, min: Float) : this(packFloats(max, min))
}

private val ScrollbarTrack.size: Float
    get() = unpackFloat1(packedValue) - unpackFloat2(packedValue)

private fun ScrollbarTrack.thumbPosition(dimension: Float): Float {
    return max(
        min(
            dimension / size,
            1f
        ),
        0f
    )
}

@Immutable
@JvmInline
value class ScrollbarStateValue internal constructor(
    internal val packedValue: Long,
)

@Composable
fun Scrollbar(
    orientation: Orientation,
    state: ScrollbarState,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
    minThumbSize: Dp = 40.dp,
    onThumbMoved: ((Float) -> Unit)? = null,
    thumb: @Composable () -> Unit
) {
    var pressedOffset by remember { mutableStateOf(Offset.Unspecified) }
    var draggedOffset by remember { mutableStateOf(Offset.Unspecified) }
    var track by remember { mutableStateOf(ScrollbarTrack(0)) }
    var interactionThumbTravelPercent by remember { mutableFloatStateOf(Float.NaN) }

    LaunchedEffect(pressedOffset) {
        Log.w("pressedOffset", pressedOffset.toString())
    }
    LaunchedEffect(draggedOffset) {
        Log.w("draggedOffset", draggedOffset.toString())
    }
    LaunchedEffect(track) {
        Log.w("track effect", "max: " + unpackFloat1(track.packedValue) + " min: " + unpackFloat2(track.packedValue))
    }
    LaunchedEffect(interactionSource) {
        interactionSource?.interactions?.collectLatest {
            Log.w("Interaction n", it.toString())
        }
    }

    Box(
        modifier = modifier
            .run {
                when (orientation) {
                    Vertical -> fillMaxHeight()
                    Horizontal -> fillMaxWidth()
                }
            }
            .onGloballyPositioned { layoutCoordinates ->
                val scrollbarStartCoordinate = orientation.valueOf(layoutCoordinates.positionInRoot())
                val length = orientation.valueOf(layoutCoordinates.size)
                track = ScrollbarTrack(
                    max = scrollbarStartCoordinate + length,
                    min = scrollbarStartCoordinate
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        try {
                            withTimeout(viewConfiguration.longPressTimeoutMillis) {
                                tryAwaitRelease()
                            }
                        } catch (e: Exception) {
                            val start = PressInteraction.Press(offset)
                            interactionSource?.tryEmit(start)
                            pressedOffset = offset
                            interactionSource?.tryEmit(
                                when {
                                    tryAwaitRelease() -> PressInteraction.Release(start)
                                    else -> PressInteraction.Cancel(start)
                                }
                            )
                            pressedOffset = Offset.Unspecified
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                var interaction: DragInteraction.Start? = null

                val onDragStart: (Offset) -> Unit = {
                    interaction = DragInteraction.Start()
                    interactionSource?.tryEmit(interaction!!)
                    draggedOffset = it
                }
                val onDragEnd: () -> Unit = {
                    interaction?.let { interactionSource?.tryEmit(DragInteraction.Stop(it)) }
                    draggedOffset = Offset.Unspecified
                }
                val onDragCancel: () -> Unit = {
                    interaction?.let { interactionSource?.tryEmit(DragInteraction.Cancel(it)) }
                    draggedOffset = Offset.Unspecified
                }
                val onDrag: (change: PointerInputChange, amount: Float) -> Unit =
                    onDrag@{ _, amount ->
                        if (draggedOffset == Offset.Unspecified) return@onDrag
                        draggedOffset = when (orientation) {
                            Vertical -> draggedOffset.copy(y = draggedOffset.y + amount)
                            Horizontal -> draggedOffset.copy(x = draggedOffset.x + amount)
                        }
                    }
                when (orientation) {
                    Vertical -> detectVerticalDragGestures(
                        onDragStart = onDragStart,
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                        onVerticalDrag = onDrag
                    )

                    Horizontal -> detectHorizontalDragGestures(
                        onDragStart = onDragStart,
                        onDragEnd = onDragEnd,
                        onDragCancel = onDragCancel,
                        onHorizontalDrag = onDrag
                    )
                }

            }
    ) {
        Layout(content = { thumb() }) { measurables, constraints ->
            val measurable = measurables.first()
            val thumbSizePx = max(
                minThumbSize.toPx(),
                state.thumbSizePercent * track.size
            )
            val trackSizePx = when (state.thumbTrackSizePercent) {
                0f -> track.size
                else -> (track.size - thumbSizePx) / state.thumbTrackSizePercent
            }

            val thumbTravelPercent = kotlin.math.max(
                a = min(
                    a = when {
                        interactionThumbTravelPercent.isNaN() -> state.thumbMovedPercent
                        else -> interactionThumbTravelPercent
                    },
                    b = state.thumbTrackSizePercent,
                ),
                b = 0f,
            )

            val thumbMovedPx = trackSizePx * thumbTravelPercent

            val y = when (orientation) {
                Horizontal -> 0
                Vertical -> thumbMovedPx.roundToInt()
            }
            val x = when (orientation) {
                Horizontal -> thumbMovedPx.roundToInt()
                Vertical -> 0
            }
            val updatedConstraints = when (orientation) {
                Vertical -> constraints.copy(
                    minHeight = thumbSizePx.roundToInt(),
                    maxHeight = thumbSizePx.roundToInt()
                )

                Horizontal -> constraints.copy(
                    minWidth = thumbSizePx.roundToInt(),
                    maxWidth = thumbSizePx.roundToInt()
                )
            }
            val placeable = measurable.measure(updatedConstraints)
            layout(minThumbSize.roundToPx(), 96.dp.roundToPx()) {
                placeable.place(x, y)
            }
        }
        if (onThumbMoved == null) return

        LaunchedEffect(Unit) {
            snapshotFlow { draggedOffset }.collect { offset ->
                if (offset == Offset.Unspecified) {
                    interactionThumbTravelPercent = Float.NaN
                    return@collect
                }

                val currentTravel = track.thumbPosition(
                    orientation.valueOf(draggedOffset)
                )
                onThumbMoved(currentTravel)
                interactionThumbTravelPercent = currentTravel
            }
        }
    }
}
