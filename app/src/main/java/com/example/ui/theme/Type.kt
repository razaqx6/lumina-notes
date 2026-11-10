package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteFontFamily

fun getFontFamily(choice: NoteFontFamily): FontFamily {
    return when (choice) {
        NoteFontFamily.SANS -> FontFamily.SansSerif
        NoteFontFamily.SERIF -> FontFamily.Serif
        NoteFontFamily.MONO -> FontFamily.Monospace
        NoteFontFamily.DYSLEXIC -> FontFamily.Default
    }
}

fun createLuminaTypography(fontFamily: FontFamily, fontSizeSp: Float = 16f, lineSpacingMultiplier: Float = 1.4f): Typography {
    val bodyBase = fontSizeSp.sp
    val bodyLine = (fontSizeSp * lineSpacingMultiplier).sp

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 38.sp,
            letterSpacing = (-0.5).sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = bodyBase,
            lineHeight = bodyLine,
            letterSpacing = 0.25.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (fontSizeSp * 0.9f).sp,
            lineHeight = (fontSizeSp * 0.9f * lineSpacingMultiplier).sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}
