package com.force.confbb.designsystem

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.force.confbb.ui.theme.ConfBBTheme
import com.force.confbb.util.ThemePreviews
import kotlinx.coroutines.launch

private const val LINES = 20
private const val PERIOD = 12000

@Composable
fun LoadingWheel(
    modifier: Modifier = Modifier.size(48.dp)
) {
    val transition = rememberInfiniteTransition("wheel")
    val startValue = if (LocalInspectionMode.current) 0f else 1f
    val floatValues = (0..LINES).map { remember { Animatable(startValue) } }
    LaunchedEffect(floatValues) {
        (0..LINES).map {
            launch {
                floatValues[it].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(
                        100,
                        easing = FastOutSlowInEasing,
                        delayMillis = 40 * it
                    ),
                )
            }
        }
    }
    val rotationAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = PERIOD,
                easing = LinearEasing
            ),
        ),
        label = "rotation"
    )

    val baseLineColor = MaterialTheme.colorScheme.onBackground
    val progressLineColor = MaterialTheme.colorScheme.inversePrimary

    val colorValues = (0..LINES).map {
        transition.animateColor(
            baseLineColor,
            baseLineColor,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = PERIOD / 2
                    progressLineColor at PERIOD / LINES / 2 using LinearEasing
                    baseLineColor at PERIOD / LINES using LinearEasing
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(PERIOD / LINES / 2 * it),
            )
        )
    }
    Canvas(
        modifier = modifier
            .padding(8.dp)
            .graphicsLayer {
                rotationZ = rotationAnim
            }
    ) {
        repeat(LINES) { index ->
            rotate(index * 360 / LINES.toFloat()) {
                drawLine(
                    color = colorValues[index].value,
                    start = Offset(size.width / 2, size.height / 4),
                    end = Offset(size.width / 2, floatValues[index].value * size.height / 4),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
fun LoadingOverlayWheel(
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(percent = 10),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
        shadowElevation = 8.dp,
        modifier = modifier
            .fillMaxSize()
    ) {
        LoadingWheel()
    }
}

@ThemePreviews
@Composable
fun LoadingOverlayPreview() {
    ConfBBTheme {
        LoadingOverlayWheel()
    }
}
