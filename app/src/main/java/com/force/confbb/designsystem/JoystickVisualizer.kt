package com.force.confbb.designsystem

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun JoystickVisualizer(
    xValue: Int,
    yValue: Int,
    modifier: Modifier = Modifier,
    pointRadius: Dp = 6.dp,
    isFast: Boolean = false
) {
    val clampedX = xValue.coerceIn(0, 4095)
    val clampedY = yValue.coerceIn(0, 4095)

    val normalizedX = (clampedX - 2048) / 2048f
    val normalizedY = (clampedY - 2048) / 2048f

    val duration = if(isFast) 1 else 100
    val animatedX by animateFloatAsState(
        targetValue = normalizedX,
        animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing),
        label = "JoystickX"
    )
    val animatedY by animateFloatAsState(
        targetValue = normalizedY,
        animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing),
        label = "JoystickY"
    )
    val color = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier) {
        val diameter = size.minDimension
        val radius = diameter / 2
        val center = Offset(size.width / 2, size.height / 2)

        val pointX = center.x + animatedX * (radius - pointRadius.toPx())
        val pointY = center.y + animatedY * (radius - pointRadius.toPx())

        drawCircle(
            color = color,
            radius = pointRadius.toPx(),
            center = Offset(pointX, pointY)
        )
    }
}
