package com.practice.todo.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.practice.todo.model.ChartData

@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    maxHeight: Float = 200f
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val maxValue = data.maxOfOrNull { it.value } ?: 1L

    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "barAnimation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Chart area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { chartData ->
                BarItem(
                    label = chartData.label,
                    value = chartData.value,
                    displayValue = chartData.displayValue,
                    maxValue = maxValue,
                    maxHeight = maxHeight,
                    barColor = barColor,
                    animationProgress = animationProgress
                )
            }
        }
    }
}

@Composable
fun BarItem(
    label: String,
    value: Long,
    displayValue: String,
    maxValue: Long,
    maxHeight: Float,
    barColor: Color,
    animationProgress: Float
) {
    val heightRatio = if (maxValue > 0) (value.toFloat() / maxValue.toFloat()) else 0f
    val barHeight = (maxHeight * heightRatio * animationProgress).coerceAtLeast(0f)

    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value display (only show if there's data)
        Box(
            modifier = Modifier.height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (value > 0) {
                Text(
                    text = displayValue,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Bar
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(barHeight.dp)
                .background(
                    color = if (value > 0) barColor else Color.Transparent,
                    shape = RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}