package com.force.confbb.designsystem

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.force.confbb.R

@Composable
fun <T : Number> NumValueSelector(
    value: T,
    onValueChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = Float.NEGATIVE_INFINITY..Float.POSITIVE_INFINITY,
    step: Double = 1.0,
    decimalPlaces: Int = 2
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(value.toString()) }
    var isError by remember { mutableStateOf(false) }

    val formattedValue = when (value) {
        is Float -> "%.${decimalPlaces}f".format(value)
        else -> value.toString()
    }

    Row(
        modifier = modifier
            .wrapContentSize()
            .border(1.dp, if (isError) Color.Red else Color.Gray, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(
            onClick = {
                val newValue = (value.toDouble() - step).coerceAtLeast(range.start.toDouble())
                onValueChange(castNumber(newValue, value))
            },
            enabled = value.toDouble() > range.start
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_minus),
                contentDescription = "Зменшити"
            )
        }

        if (isEditing) {
            TextField(
                value = editText,
                onValueChange = {
                    editText = it
                    val entered = it.replace(",", ".").toDoubleOrNull()
                    isError = entered == null || entered !in range
                },
                modifier = Modifier
                    .widthIn(min = 60.dp)
                    .padding(0.dp),
                singleLine = true,
                isError = isError,
                textStyle = LocalTextStyle.run {
                    current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                keyboardActions = KeyboardActions(onDone = {
                    val entered = editText.replace(",", ".").toDoubleOrNull()
                    if (entered != null && entered in range) {
                        onValueChange(castNumber(entered, value))
                        isEditing = false
                    } else {
                        isError = true
                    }
                })
            )
        } else {
            Text(
                text = formattedValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                //todo make it clickable to enter number with keyboard
                /*
                .clickable {
                    isEditing = true
                    editText = formattedValue
                    isError = false
                }
                 */
            )
        }

        IconButton(
            onClick = {
                val newValue = (value.toDouble() + step).coerceAtMost(range.endInclusive.toDouble())
                onValueChange(castNumber(newValue, value))
            },
            enabled = value.toDouble() < range.endInclusive
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus),
                contentDescription = "Збільшити"
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Number> castNumber(value: Double, original: T): T {
    return when (original) {
        is Int -> value.toInt() as T
        is Float -> value.toFloat() as T
        is Double -> value as T
        else -> throw IllegalArgumentException("Unsupported number type")
    }
}
