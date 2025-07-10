package com.force.confbb.designsystem

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItemColors
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.force.confbb.ui.theme.ConfBBTheme
import com.force.confbb.util.ThemePreviews

@Composable
fun RowScope.MyNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable (() -> Unit)? = null,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    alwaysShowLabel: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        label = label,
        icon = if (selected) selectedIcon else icon,
        alwaysShowLabel = alwaysShowLabel,
        enabled = enabled,
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun MyNavigationBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 0.dp,
        content = content
    )
}


@Composable
fun MyNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable (() -> Unit)? = null,
    icon: @Composable () -> Unit,
    selectedIcon: @Composable () -> Unit = icon,
    alwaysShowLabel: Boolean = true,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        label = label,
        icon = if (selected) selectedIcon else icon,
        alwaysShowLabel = alwaysShowLabel,
        enabled = enabled,
        modifier = modifier,
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

@Composable
fun MyNavigationRail(
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    NavigationRail(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        header = header,
        content = content,
    )
}

@Composable
fun MyNavigationSuiteScaffold(
    items: MyNavigationSuiteScaffoldScope.() -> Unit,
    modifier: Modifier = Modifier,
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(),
    content: @Composable () -> Unit
) {
    val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)
    val navigationSuiteItemColors = NavigationSuiteItemColors(
        navigationBarItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationRailItemColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        navigationDrawerItemColors = NavigationDrawerItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            MyNavigationSuiteScaffoldScope(
                navigationSuiteScope = this,
                navigationSuiteItemColors
            ).run(items)
        },
        modifier = modifier,
        layoutType = layoutType,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationRailContainerColor = Color.Transparent,
        ),
        containerColor = Color.Transparent,
    ) {
        content()
    }
}

class MyNavigationSuiteScaffoldScope internal constructor(
    private val navigationSuiteScope: NavigationSuiteScope,
    private val navigationSuiteItemColors: NavigationSuiteItemColors
) {
    fun item(
        selected: Boolean,
        onClick: () -> Unit,
        icon: @Composable () -> Unit,
        selectedIcon: @Composable () -> Unit = icon,
        modifier: Modifier = Modifier,
        label: @Composable (() -> Unit)? = null,
    ) = navigationSuiteScope.item(
        selected = selected,
        onClick = onClick,
        icon = if (selected) {
            selectedIcon
        } else {
            icon
        },
        modifier = modifier,
        label = label,
        colors = navigationSuiteItemColors,
    )
}

@ThemePreviews
@Composable
fun NavigationBarPreview() {
    val items = listOf("one", "two", "three")
    val icons = listOf(Icons.Rounded.DateRange, Icons.Rounded.ShoppingCart, Icons.Rounded.Settings)
    val selectedIcons = listOf(Icons.Outlined.DateRange, Icons.Outlined.ShoppingCart, Icons.Outlined.Settings)
    ConfBBTheme(
        dynamicColor = false
    ) {
        MyNavigationBar {
            items.forEachIndexed { index, item ->
                MyNavigationBarItem(
                    index == 0,
                    onClick = {},
                    label = { Text(item) },
                    icon = { Icon(imageVector = icons[index], contentDescription = null) },
                    selectedIcon = { Icon(imageVector = selectedIcons[index], contentDescription = null) }
                )
            }
        }
    }
}


@ThemePreviews
@Composable
fun NavigationRailPreview() {
    val items = listOf("one", "two", "three")
    val icons = listOf(Icons.Rounded.DateRange, Icons.Rounded.ShoppingCart, Icons.Rounded.Settings)
    val selectedIcons = listOf(Icons.Outlined.DateRange, Icons.Outlined.ShoppingCart, Icons.Outlined.Settings)
    ConfBBTheme(
        dynamicColor = false
    ) {
        MyNavigationRail {
            items.forEachIndexed { index, item ->
                MyNavigationRailItem(
                    index == 0,
                    onClick = {},
                    label = { Text(item) },
                    icon = { Icon(imageVector = icons[index], contentDescription = null) },
                    selectedIcon = { Icon(imageVector = selectedIcons[index], contentDescription = null) }
                )
            }
        }
    }
}
