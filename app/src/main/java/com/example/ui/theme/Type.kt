package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val LoraEditorialFamily = KissatenEditorial

val SpaceGroteskFamily = KissatenBody

val PlusJakartaFamily = KissatenBody

val JetBrainsMonoFamily = FontFamily.Monospace

fun buildReturnmarkTypography(scale: Float = 1.0f): Typography {
    val s = scale.coerceIn(0.85f, 1.35f)
    return Typography(
        displayLarge = TextStyle(
            fontFamily = LoraEditorialFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (36 * s).sp,
            lineHeight = (44 * s).sp,
            letterSpacing = (-0.3).sp
        ),
        displayMedium = TextStyle(
            fontFamily = LoraEditorialFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * s).sp,
            lineHeight = (35 * s).sp,
            letterSpacing = (-0.2).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = LoraEditorialFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * s).sp,
            lineHeight = (31 * s).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = LoraEditorialFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * s).sp,
            lineHeight = (27 * s).sp
        ),
        titleLarge = TextStyle(
            fontFamily = LoraEditorialFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * s).sp,
            lineHeight = (24 * s).sp
        ),
        titleMedium = TextStyle(
            fontFamily = PlusJakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * s).sp,
            lineHeight = (22 * s).sp
        ),
        titleSmall = TextStyle(
            fontFamily = PlusJakartaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = PlusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * s).sp,
            lineHeight = (22 * s).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = PlusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * s).sp,
            lineHeight = (20 * s).sp
        ),
        bodySmall = TextStyle(
            fontFamily = PlusJakartaFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * s).sp,
            lineHeight = (17 * s).sp
        ),
        labelLarge = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * s).sp,
            lineHeight = (18 * s).sp
        ),
        labelMedium = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * s).sp,
            lineHeight = (16 * s).sp
        ),
        labelSmall = TextStyle(
            fontFamily = JetBrainsMonoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (10 * s).sp,
            lineHeight = (14 * s).sp
        )
    )
}

val Typography = buildReturnmarkTypography(1.0f)
