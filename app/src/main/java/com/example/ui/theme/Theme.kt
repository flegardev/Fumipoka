package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.ReturnmarkThemeMode
import com.example.data.ThemeAppearanceMode

val ReturnmarkShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun ReturnmarkTheme(
    themeMode: ReturnmarkThemeMode = ReturnmarkThemeMode.LAVENDER,
    appearanceMode: ThemeAppearanceMode = ThemeAppearanceMode.LIGHT,
    customAccentHex: String = "#8B6FE8",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val colors = resolveReturnmarkColors(context, themeMode, appearanceMode, customAccentHex, systemDark)
    val effectiveDark = colors.background.luminance() < 0.5f
    androidx.compose.runtime.SideEffect {
        (context as? android.app.Activity)?.let { activity ->
            androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
                isAppearanceLightStatusBars = !effectiveDark
                isAppearanceLightNavigationBars = !effectiveDark
            }
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = buildReturnmarkTypography(fontScale),
        shapes = ReturnmarkShapes,
        content = content
    )
}

/** Shared by Compose and native widgets so all legacy and custom palettes agree. */
fun resolveReturnmarkColors(
    context: android.content.Context,
    themeMode: ReturnmarkThemeMode,
    appearanceMode: ThemeAppearanceMode,
    customAccentHex: String,
    systemDark: Boolean
): ColorScheme {
    // The legacy OLED palette remains a forced-black palette even if the user
    // subsequently switches the separate appearance control to Light.
    val effectiveDark = themeMode == ReturnmarkThemeMode.DARK_OLED || when (appearanceMode) {
        ThemeAppearanceMode.LIGHT -> false
        ThemeAppearanceMode.DARK -> true
        ThemeAppearanceMode.OLED -> true
        ThemeAppearanceMode.SYSTEM -> systemDark
    }
    val effectiveOled = appearanceMode == ThemeAppearanceMode.OLED || themeMode == ReturnmarkThemeMode.DARK_OLED

    val tokens = getPastelPaletteTokens(themeMode)
    val defaultThemeAccent = if (effectiveDark) tokens.darkPrimary else tokens.lightPrimary
    val userAccent = if (customAccentHex.equals(themeMode.previewAccentHex, ignoreCase = true)) defaultThemeAccent else parseHexColorSafely(customAccentHex, defaultThemeAccent)

    // Ensure high legibility when user picks a custom accent in Light vs Dark mode
    val resolvedPrimary = if (!effectiveDark && userAccent.luminance() > 0.72f) {
        tokens.lightPrimary
    } else {
        userAccent
    }
    val resolvedOnPrimary = if (resolvedPrimary.luminance() > 0.45f) Color(0xFF0E1015) else Color.White

    val baseScheme: ColorScheme = when (themeMode) {
        ReturnmarkThemeMode.MATERIAL_YOU -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (effectiveDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                buildPastelScheme(tokens, resolvedPrimary, resolvedOnPrimary, effectiveDark, effectiveOled)
            }
        }

        ReturnmarkThemeMode.HIGH_CONTRAST -> {
            if (effectiveDark) {
                darkColorScheme(
                    primary = HighContrastYellow,
                    onPrimary = Color.Black,
                    primaryContainer = Color(0xFF333300),
                    onPrimaryContainer = HighContrastYellow,
                    secondary = HighContrastCyan,
                    onSecondary = Color.Black,
                    background = Color.Black,
                    onBackground = Color.White,
                    surface = Color(0xFF080808),
                    onSurface = Color.White,
                    surfaceVariant = Color(0xFF1A1A1A),
                    onSurfaceVariant = Color.White,
                    outline = HighContrastYellow
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF000000),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFFFF275),
                    onPrimaryContainer = Color.Black,
                    secondary = Color(0xFF004E89),
                    onSecondary = Color.White,
                    background = Color.White,
                    onBackground = Color.Black,
                    surface = Color(0xFFF7F7F7),
                    onSurface = Color.Black,
                    surfaceVariant = Color(0xFFE8E8E8),
                    onSurfaceVariant = Color.Black,
                    outline = Color.Black
                )
            }
        }

        ReturnmarkThemeMode.FOREST_GREEN -> {
            if (effectiveDark) {
                darkColorScheme(
                    primary = BotanicalLime,
                    onPrimary = Color(0xFF06150E),
                    primaryContainer = Color(0xFF1B4332),
                    onPrimaryContainer = BotanicalLime,
                    secondary = ElectricLime,
                    onSecondary = Color(0xFF0B1408),
                    tertiary = Color(0xFF38BDF8),
                    background = if (effectiveOled) OledBlack else ForestBg,
                    onBackground = OffWhiteInk,
                    surface = if (effectiveOled) OledSurface else ForestSurface,
                    onSurface = OffWhiteInk,
                    surfaceVariant = if (effectiveOled) OledSurfaceVariant else ForestSurfaceVariant,
                    onSurfaceVariant = Color(0xFFA7C4B5),
                    outline = Color(0xFF285243)
                )
            } else {
                buildPastelScheme(getPastelPaletteTokens(ReturnmarkThemeMode.MINT), resolvedPrimary, resolvedOnPrimary, false, false)
            }
        }

        ReturnmarkThemeMode.ELECTRIC_LIME -> {
            if (effectiveDark) {
                darkColorScheme(
                    primary = VoltAccent,
                    onPrimary = Color(0xFF090B06),
                    primaryContainer = Color(0xFF273114),
                    onPrimaryContainer = VoltAccent,
                    secondary = MintEmerald,
                    onSecondary = Color(0xFF061610),
                    background = if (effectiveOled) OledBlack else VoltBg,
                    onBackground = OffWhiteInk,
                    surface = if (effectiveOled) OledSurface else VoltSurface,
                    onSurface = OffWhiteInk,
                    surfaceVariant = if (effectiveOled) OledSurfaceVariant else VoltSurfaceVariant,
                    onSurfaceVariant = Color(0xFFB3C2A8),
                    outline = Color(0xFF33402A)
                )
            } else {
                buildPastelScheme(tokens, resolvedPrimary, resolvedOnPrimary, false, false)
            }
        }

        else -> buildPastelScheme(tokens, resolvedPrimary, resolvedOnPrimary, effectiveDark, effectiveOled)
    }

    return kissatenColors(baseScheme, effectiveOled)
}

private fun buildPastelScheme(
    tokens: PastelPaletteTokens,
    primaryColor: Color,
    onPrimaryColor: Color,
    isDark: Boolean,
    isOled: Boolean
): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = tokens.darkPrimaryContainer,
            onPrimaryContainer = tokens.darkOnPrimaryContainer,
            secondary = tokens.darkSecondary,
            onSecondary = Color(0xFF081218),
            tertiary = tokens.darkTertiary,
            background = if (isOled) OledBlack else tokens.darkBg,
            onBackground = if (isOled) Color.White else tokens.darkInk,
            surface = if (isOled) OledSurface else tokens.darkSurface,
            onSurface = if (isOled) Color.White else tokens.darkInk,
            surfaceVariant = if (isOled) OledSurfaceVariant else tokens.darkSurfaceVariant,
            onSurfaceVariant = tokens.darkSubtleInk,
            outline = if (isOled) Color(0xFF252832) else tokens.darkOutline
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = tokens.lightPrimaryContainer,
            onPrimaryContainer = tokens.lightOnPrimaryContainer,
            secondary = tokens.lightSecondary,
            onSecondary = Color.White,
            tertiary = tokens.lightTertiary,
            background = tokens.lightBg,
            onBackground = tokens.lightInk,
            surface = tokens.lightSurface,
            onSurface = tokens.lightInk,
            surfaceVariant = tokens.lightSurfaceVariant,
            onSurfaceVariant = tokens.lightSubtleInk,
            outline = tokens.lightOutline
        )
    }
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    ReturnmarkTheme(content = content)
}
