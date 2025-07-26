package com.force.confbb.feature.info

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.force.confbb.R
import com.force.confbb.analytics.AnalyticsLogger
import com.force.confbb.designsystem.LoadingWheel
import kotlinx.serialization.Serializable

@Serializable
object InfoRoute

fun NavController.navigateToInfo(navOptions: NavOptions) {
    navigate(InfoRoute, navOptions)
}

@Composable
fun Info() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnalyticsLogger.logScreenView("info_screen")
        Box(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            LoadingWheel(
                modifier = Modifier
                    .size(480.dp)
                    .align(Alignment.TopCenter)
                    .alpha(0.2f)
            )
            LoadingWheel(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopStart)
                    .offset(x = 120.dp, y = 270.dp)
                    .alpha(0.2f)
            )
            LoadingWheel(
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-120).dp, y = 270.dp)
                    .alpha(0.2f)
            )
            LoadingWheel(
                modifier = Modifier
                    .size(width = 160.dp, height = 40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-270).dp)
                    .alpha(0.2f)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.about),
                )
            }
            val context = LocalContext.current
            val telegramUrl = "https://t.me/dn_rd"
            Text(
                text = "Служба підтримки",
                modifier = Modifier
                    .padding(16.dp)
                    .clickable {
                        AnalyticsLogger.logButtonClicked("support")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                        context.startActivity(intent)
                    },
                color = Color.Red,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

