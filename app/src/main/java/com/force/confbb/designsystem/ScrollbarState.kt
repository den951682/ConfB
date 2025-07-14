package com.force.confbb.designsystem

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemInfo
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ScrollbarState {
    private var packedValue by mutableLongStateOf(0L)

    internal fun onScroll(stateValue: ScrollbarStateValue) {
        packedValue = stateValue.packedValue
    }

    val thumbSizePercent
        get() = unpackFloat1(packedValue)

    val thumbTrackSizePercent
        get() = 1 - thumbSizePercent

    val thumbMovedPercent
        get() = unpackFloat2(packedValue)
}

@Composable
fun LazyStaggeredGridState.scrollbarState(
    itemsAvailable: Int,
    itemIndex: (LazyStaggeredGridItemInfo) -> Int = LazyStaggeredGridItemInfo::index,
): ScrollbarState {
    val state = remember { ScrollbarState() }
    LaunchedEffect(this, itemsAvailable) {
        snapshotFlow {
            if (itemsAvailable == 0) return@snapshotFlow null

            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@snapshotFlow null

            val firstIndex = min(
                a = interpolateFirstItemIndex(
                    visibleItems = visibleItemsInfo,
                    itemSize = { layoutInfo.orientation.valueOf(it.size) },
                    offset = { layoutInfo.orientation.valueOf(it.offset) },
                    nextItemOnMainAxis = { first ->
                        visibleItemsInfo.find { it != first && it.lane == first.lane }
                    },
                    itemIndex = itemIndex,
                ),
                b = itemsAvailable.toFloat(),
            )
            if (firstIndex.isNaN()) return@snapshotFlow null

            val itemsVisible = visibleItemsInfo.floatSumOf { itemInfo ->
                itemVisibilityPercentage(
                    itemSize = layoutInfo.orientation.valueOf(itemInfo.size),
                    itemStartOffset = layoutInfo.orientation.valueOf(itemInfo.offset),
                    viewportStartOffset = layoutInfo.viewportStartOffset,
                    viewportEndOffset = layoutInfo.viewportEndOffset,
                )
            }

            val thumbTravelPercent = min(
                a = firstIndex / itemsAvailable,
                b = 1f,
            )
            val thumbSizePercent = min(
                a = itemsVisible / itemsAvailable,
                b = 1f,
            )
            scrollbarStateValue(
                thumbSizePercent = thumbSizePercent,
                thumbMovedPercent = thumbTravelPercent,
            )
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { state.onScroll(it) }
    }
    return state
}

fun scrollbarStateValue(
    thumbSizePercent: Float,
    thumbMovedPercent: Float,
) = ScrollbarStateValue(
    packFloats(
        val1 = thumbSizePercent,
        val2 = thumbMovedPercent,
    ),
)

internal fun itemVisibilityPercentage(
    itemSize: Int,
    itemStartOffset: Int,
    viewportStartOffset: Int,
    viewportEndOffset: Int,
): Float {
    if (itemSize == 0) return 0f
    val itemEnd = itemStartOffset + itemSize
    val startOffset = when {
        itemStartOffset > viewportStartOffset -> 0
        else -> abs(abs(viewportStartOffset) - abs(itemStartOffset))
    }
    val endOffset = when {
        itemEnd < viewportEndOffset -> 0
        else -> abs(abs(itemEnd) - abs(viewportEndOffset))
    }
    val size = itemSize.toFloat()
    return (size - startOffset - endOffset) / size
}

private inline fun <T> List<T>.floatSumOf(selector: (T) -> Float): Float =
    fold(initial = 0f) { accumulator, listItem -> accumulator + selector(listItem) }

internal inline fun <LazyState : ScrollableState, LazyStateItem> LazyState.interpolateFirstItemIndex(
    visibleItems: List<LazyStateItem>,
    crossinline itemSize: LazyState.(LazyStateItem) -> Int,
    crossinline offset: LazyState.(LazyStateItem) -> Int,
    crossinline nextItemOnMainAxis: LazyState.(LazyStateItem) -> LazyStateItem?,
    crossinline itemIndex: (LazyStateItem) -> Int
): Float {
    if (visibleItems.isEmpty()) return 0f

    val firstItem = visibleItems.first()
    val firstItemIndex = itemIndex(firstItem)

    if (firstItemIndex < 0) return Float.NaN

    val firstItemSize = itemSize(firstItem)
    if (firstItemSize == 0) return Float.NaN

    val itemOffset = offset(firstItem).toFloat()
    val offsetPercentage = abs(itemOffset) / firstItemSize

    val nextItem = nextItemOnMainAxis(firstItem) ?: return firstItemIndex + offsetPercentage

    val nextItemIndex = itemIndex(nextItem)

    return firstItemIndex + ((nextItemIndex - firstItemIndex) * offsetPercentage)
}
