package com.force.confbb.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun LazyStaggeredGridState.rememberDraggableScroller(itemsAvailable: Int): (Float) -> Unit {
    return rememberDraggableScroller(
        itemsAvailable,
        scroll = ::scrollToItem
    )
}

@Composable
inline fun rememberDraggableScroller(
    itemsAvailable: Int,
    crossinline scroll: suspend (Int) -> Unit
): (Float) -> Unit {
    var percentage by remember { mutableFloatStateOf(Float.NaN) }
    val itemCount by rememberUpdatedState(itemsAvailable)
    LaunchedEffect(percentage) {
        if (percentage.isNaN()) return@LaunchedEffect
        val indexToScroll = (itemCount * percentage).roundToInt()
        scroll(indexToScroll)
    }
    return { newPercentage -> percentage = newPercentage }
}

@Composable
fun ScrollableState.DraggableScrollbar(
    modifier: Modifier,
    orientation: Orientation,
    scrollbarState: ScrollbarState,
    onThumbMoved: ((Float) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    Scrollbar(
        orientation = orientation,
        state = scrollbarState,
        modifier = modifier,
        interactionSource = interactionSource,
        onThumbMoved = onThumbMoved,
        thumb = {
            DraggableThumb(interactionSource, orientation)
        }
    )
}

@Composable
fun ScrollableState.DraggableThumb(
    interactionSource: InteractionSource,
    orientation: Orientation
) {
    Box(
        modifier = Modifier
            .run {
                when (orientation) {
                    Orientation.Vertical -> width(12.dp).fillMaxHeight()
                    Orientation.Horizontal -> height(12.dp).fillMaxWidth()
                }
            }
            .scrollThumb(this, interactionSource)
    )
}

@Composable
fun Modifier.scrollThumb(
    scrollbarState: ScrollableState,
    interactionSource: InteractionSource
): Modifier {
    val colorState = scrollbarThumbColor(scrollbarState, interactionSource)
    return this then ScrollThumbElement { colorState.value }
}

private data class ScrollThumbElement(val colorProducer: ColorProducer) : ModifierNodeElement<ScrollThumbNode>() {
    override fun create() = ScrollThumbNode(colorProducer)

    override fun update(node: ScrollThumbNode) {
        node.colorProducer = colorProducer
        node.invalidateDraw()
    }

}

private class ScrollThumbNode(var colorProducer: ColorProducer) : DrawModifierNode, Modifier.Node() {
    private val shape = RoundedCornerShape(16.dp)
    private var lastSize: Size? = null
    private var lastLayoutDirection: LayoutDirection? = null
    private var lastOutline: Outline? = null

    override fun ContentDrawScope.draw() {
        val color = colorProducer.invoke()
        val outline = if (size == lastSize && layoutDirection == lastLayoutDirection) {
            lastOutline!!
        } else {
            shape.createOutline(size, layoutDirection, this)
        }
        lastSize = size
        lastLayoutDirection = layoutDirection
        lastOutline = outline
        if (color != Color.Unspecified) drawOutline(lastOutline!!, color)
    }
}

@Composable
private fun scrollbarThumbColor(
    scrollbarState: ScrollableState,
    interactionSource: InteractionSource
): State<Color> {
    var state by remember { mutableStateOf(ThumbState.DORMANT) }
    val pressed by interactionSource.collectIsPressedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val dragged by interactionSource.collectIsDraggedAsState()
    val active = (scrollbarState.canScrollForward || scrollbarState.canScrollBackward) &&
            (pressed || dragged || hovered || scrollbarState.isScrollInProgress)
    val color = animateColorAsState(
        when (state) {
            ThumbState.ACTIVE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            ThumbState.NOT_ACTIVE -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            ThumbState.DORMANT -> Color.Transparent
        },
        animationSpec = SpringSpec(
            stiffness = Spring.StiffnessLow,
        ),
        label = "Scrollbar thumb color",
    )
    LaunchedEffect(active) {
        if (active) {
            state = ThumbState.ACTIVE
        } else {
            state = ThumbState.NOT_ACTIVE
            delay(1000)
            state = ThumbState.DORMANT
        }
    }
    return color
}

enum class ThumbState {
    ACTIVE,
    NOT_ACTIVE,
    DORMANT
}
