package com.force.confbb.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.force.confbb.R

@Composable
fun NumValueSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = Int.MIN_VALUE..Int.MAX_VALUE,
    step: Int = 1
) {
    Row(
        modifier = modifier
            .wrapContentSize()
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = {
                val newValue = (value - step).coerceAtLeast(range.first)
                onValueChange(newValue)
            },
            enabled = value > range.first
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_minus),
                contentDescription = "Зменшити"
            )
        }

        Text(
            text = value.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        IconButton(
            onClick = {
                val newValue = (value + step).coerceAtMost(range.last)
                onValueChange(newValue)
            },
            enabled = value < range.last
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = "Збільшити"
            )
        }
    }
}
