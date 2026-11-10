package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.LiquidColorScheme
import com.example.data.model.UserPreferences

@Composable
fun LuminaNoteTheme(
    preferences: UserPreferences = UserPreferences(),
    content: @Composable () -> Unit
) {
    val isDark = preferences.isDarkMode
    val isSepia = preferences.isEyeComfortSepia
    val schemeChoice = preferences.colorScheme

    val primaryColor = Color(schemeChoice.primaryHex)
    val glowColor = Color(schemeChoice.glowHex)

    val colorScheme = when {
        isSepia -> {
            lightColorScheme(
                primary = Color(0xFF8D6E63),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFD7CCC8),
                onPrimaryContainer = LiquidColors.SepiaTextPrimary,
                secondary = Color(0xFFA1887F),
                background = LiquidColors.SepiaBackground,
                onBackground = LiquidColors.SepiaTextPrimary,
                surface = LiquidColors.SepiaSurface,
                onSurface = LiquidColors.SepiaTextPrimary,
                surfaceVariant = Color(0xFFEFEBE9),
                onSurfaceVariant = LiquidColors.SepiaTextSecondary,
                outline = LiquidColors.SepiaBorder
            )
        }
        isDark -> {
            darkColorScheme(
                primary = glowColor,
                onPrimary = Color.Black,
                primaryContainer = primaryColor.copy(alpha = 0.35f),
                onPrimaryContainer = glowColor,
                secondary = glowColor.copy(alpha = 0.85f),
                background = Color(0xFF090D16),
                onBackground = Color(0xFFF1F5F9),
                surface = Color(0xFF111827),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF1F2937),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = Color(0x33FFFFFF)
            )
        }
        else -> {
            // Light Mode (One UI & iOS 27 modern sleek)
            lightColorScheme(
                primary = primaryColor,
                onPrimary = Color.White,
                primaryContainer = primaryColor.copy(alpha = 0.15f),
                onPrimaryContainer = primaryColor,
                secondary = glowColor,
                background = Color(0xFFF8FAFC),
                onBackground = Color(0xFF0F172A),
                surface = Color(0xFFFFFFFF),
                onSurface = Color(0xFF0F172A),
                surfaceVariant = Color(0xFFF1F5F9),
                onSurfaceVariant = Color(0xFF64748B),
                outline = Color(0x26000000)
            )
        }
    }

    val font = getFontFamily(preferences.fontFamily)
    val typography = createLuminaTypography(
        fontFamily = font,
        fontSizeSp = preferences.fontSizeSp,
        lineSpacingMultiplier = preferences.lineSpacingMultiplier
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
