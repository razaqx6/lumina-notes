package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 4.dp
): Modifier {
    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.08f)
        )
        .clip(shape)
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor.copy(alpha = (backgroundColor.alpha * 1.1f).coerceAtMost(0.95f)),
                    backgroundColor.copy(alpha = (backgroundColor.alpha * 0.85f).coerceAtLeast(0.4f))
                )
            )
        )
        .border(
            width = borderWidth,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f),
                    borderColor,
                    Color.White.copy(alpha = 0.15f)
                )
            ),
            shape = shape
        )
}

@Composable
fun Modifier.liquidGlassPill(
    isSelected: Boolean,
    accentColor: Color,
    shape: Shape = RoundedCornerShape(50)
): Modifier {
    val bg = if (isSelected) {
        accentColor.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    }
    val borderCol = if (isSelected) {
        accentColor.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }

    return this
        .clip(shape)
        .background(bg)
        .border(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderCol,
            shape = shape
        )
}

@Composable
fun Modifier.liquidGlassGradientBackground(
    primaryColor: Color,
    isDark: Boolean,
    isSepia: Boolean
): Modifier {
    val backgroundBrush = when {
        isSepia -> Brush.verticalGradient(
            colors = listOf(
                LiquidColors.SepiaBackground,
                Color(0xFFF3E9D2),
                LiquidColors.SepiaBackground
            )
        )
        isDark -> Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.18f),
                Color(0xFF0F172A),
                Color(0xFF070B14)
            ),
            radius = 1400f
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.12f),
                Color(0xFFF8FAFC),
                Color(0xFFF1F5F9)
            )
        )
    }
    return this.background(backgroundBrush)
}
