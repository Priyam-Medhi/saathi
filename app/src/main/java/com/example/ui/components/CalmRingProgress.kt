package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.SageGreen
import java.util.Locale

@Composable
fun CalmRingProgress(
    percentage: Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    size: Dp = 110.dp,
    showPercentageText: Boolean = true,
    customColor: Color? = null,
    sublabel: String? = null
) {
    val ringColor = customColor ?: when {
        percentage >= 78.0 -> SageGreen
        percentage >= 75.0 -> CautionMustard
        else -> BelowTerracotta
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (percentage.coerceIn(0.0, 100.0) / 100f).toFloat(),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "RingProgress"
    )

    val trackColor = ringColor.copy(alpha = 0.18f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.toPx() - strokePx
            val radius = diameter / 2f
            val centerOffset = androidx.compose.ui.geometry.Offset(size.toPx() / 2f, size.toPx() / 2f)

            // Background Track Arc
            drawCircle(
                color = trackColor,
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Progress Arc (Starts from top - 270 deg)
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2f, strokePx / 2f),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }

        if (showPercentageText) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format(Locale.US, "%.0f%%", percentage),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.22f).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (!sublabel.isNullOrEmpty()) {
                    Text(
                        text = sublabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalMuted,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }
    }
}
