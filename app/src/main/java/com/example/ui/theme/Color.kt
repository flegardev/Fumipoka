package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.ReturnmarkThemeMode

// Classic Studio Tokens (Preserved)
val MidnightBg = Color(0xFF0E1013)
val MidnightSurface = Color(0xFF16191E)
val MidnightSurfaceVariant = Color(0xFF1F242D)
val ElectricLime = Color(0xFFCCFF00)
val ElectricLimeDark = Color(0xFF99CC00)
val MintEmerald = Color(0xFF34D399)
val OffWhiteInk = Color(0xFFF4F4F0)
val MutedSlateText = Color(0xFF9CA3AF)

// Soft Ivory (Warm Editorial Paper)
val IvoryBg = Color(0xFFFAF8F5)
val IvorySurface = Color(0xFFF2EFE9)
val IvorySurfaceVariant = Color(0xFFE6E1D6)
val ForestInk = Color(0xFF1B4332)
val CharcoalInk = Color(0xFF18191B)
val WarmGrayText = Color(0xFF57534E)

// Forest Green (Botanical Pine)
val ForestBg = Color(0xFF0A1612)
val ForestSurface = Color(0xFF12261F)
val ForestSurfaceVariant = Color(0xFF1B382E)
val BotanicalLime = Color(0xFF7CE38B)

// Electric Lime Studio
val VoltBg = Color(0xFF0B0D0A)
val VoltSurface = Color(0xFF141812)
val VoltSurfaceVariant = Color(0xFF1E241B)
val VoltAccent = Color(0xFFD9FF00)

// True Dark OLED
val OledBlack = Color(0xFF000000)
val OledSurface = Color(0xFF0B0C0E)
val OledSurfaceVariant = Color(0xFF15181E)

// High Contrast WCAG AAA
val HighContrastYellow = Color(0xFFFFFF00)
val HighContrastCyan = Color(0xFF00FFFF)

// =========================================================================
// RETURNMARK 3.0 — SOFT, BRIGHT PASTEL PALETTES (LIGHT, DARK & TRUE OLED)
// =========================================================================

data class PastelPaletteTokens(
    val lightPrimary: Color,
    val lightOnPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    val lightTertiary: Color,
    val lightBg: Color,
    val lightSurface: Color,
    val lightSurfaceVariant: Color,
    val lightInk: Color,
    val lightSubtleInk: Color,
    val lightOutline: Color,
    val darkPrimary: Color,
    val darkOnPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val darkSecondary: Color,
    val darkTertiary: Color,
    val darkBg: Color,
    val darkSurface: Color,
    val darkSurfaceVariant: Color,
    val darkInk: Color,
    val darkSubtleInk: Color,
    val darkOutline: Color
)

fun getPastelPaletteTokens(mode: ReturnmarkThemeMode): PastelPaletteTokens {
    return when (mode) {
        ReturnmarkThemeMode.SAKURA_BLUSH -> PastelPaletteTokens(
            lightPrimary = Color(0xFFC86D85),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFF9E4EA),
            lightOnPrimaryContainer = Color(0xFF4A1D2B),
            lightSecondary = Color(0xFF9C7AA8),
            lightTertiary = Color(0xFFD4926A),
            lightBg = Color(0xFFFCF7F7),
            lightSurface = Color(0xFFFFFCFC),
            lightSurfaceVariant = Color(0xFFF4E9EC),
            lightInk = Color(0xFF2C2227),
            lightSubtleInk = Color(0xFF6E5A63),
            lightOutline = Color(0xFFE6D1D8),
            darkPrimary = Color(0xFFF2A7BC),
            darkOnPrimary = Color(0xFF38121E),
            darkPrimaryContainer = Color(0xFF4D2231),
            darkOnPrimaryContainer = Color(0xFFFFE3EC),
            darkSecondary = Color(0xFFC8B2E8),
            darkTertiary = Color(0xFFF3BE8A),
            darkBg = Color(0xFF161318),
            darkSurface = Color(0xFF211C24),
            darkSurfaceVariant = Color(0xFF2E2633),
            darkInk = Color(0xFFF9F1F4),
            darkSubtleInk = Color(0xFFC2AFB8),
            darkOutline = Color(0xFF46394D)
        )

        ReturnmarkThemeMode.MATCHA_MIST -> PastelPaletteTokens(
            lightPrimary = Color(0xFF598B6E),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFE1EFE6),
            lightOnPrimaryContainer = Color(0xFF1B3B29),
            lightSecondary = Color(0xFF7A9478),
            lightTertiary = Color(0xFFC48B5C),
            lightBg = Color(0xFFF6F8F4),
            lightSurface = Color(0xFFFCFDFB),
            lightSurfaceVariant = Color(0xFFE9EFE7),
            lightInk = Color(0xFF212B25),
            lightSubtleInk = Color(0xFF57695E),
            lightOutline = Color(0xFFCFDCD2),
            darkPrimary = Color(0xFF95CFA9),
            darkOnPrimary = Color(0xFF0E2B1B),
            darkPrimaryContainer = Color(0xFF1F422F),
            darkOnPrimaryContainer = Color(0xFFDDF5E5),
            darkSecondary = Color(0xFFA9C9B8),
            darkTertiary = Color(0xFFEBB688),
            darkBg = Color(0xFF111714),
            darkSurface = Color(0xFF19221E),
            darkSurfaceVariant = Color(0xFF23302A),
            darkInk = Color(0xFFEEF5F0),
            darkSubtleInk = Color(0xFFA2B8AB),
            darkOutline = Color(0xFF34473E)
        )

        ReturnmarkThemeMode.KYOTO_SKY -> PastelPaletteTokens(
            lightPrimary = Color(0xFF5386B6),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFE2EEF8),
            lightOnPrimaryContainer = Color(0xFF183654),
            lightSecondary = Color(0xFF7B8FA8),
            lightTertiary = Color(0xFFC97B84),
            lightBg = Color(0xFFF5F8FB),
            lightSurface = Color(0xFFFCFDFF),
            lightSurfaceVariant = Color(0xFFE8EFF6),
            lightInk = Color(0xFF1E2833),
            lightSubtleInk = Color(0xFF556577),
            lightOutline = Color(0xFFCBD9E8),
            darkPrimary = Color(0xFF93C2F0),
            darkOnPrimary = Color(0xFF0C253D),
            darkPrimaryContainer = Color(0xFF1E3B59),
            darkOnPrimaryContainer = Color(0xFFDCEEFF),
            darkSecondary = Color(0xFFB3C7E6),
            darkTertiary = Color(0xFFF0A6B0),
            darkBg = Color(0xFF11161F),
            darkSurface = Color(0xFF18202C),
            darkSurfaceVariant = Color(0xFF222D3E),
            darkInk = Color(0xFFEEF4FA),
            darkSubtleInk = Color(0xFFA2B3C7),
            darkOutline = Color(0xFF34445B)
        )

        ReturnmarkThemeMode.LANTERN_AMBER -> PastelPaletteTokens(
            lightPrimary = Color(0xFFC97A3E),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFFAEBDD),
            lightOnPrimaryContainer = Color(0xFF4D260A),
            lightSecondary = Color(0xFF9E7B66),
            lightTertiary = Color(0xFF6B8E78),
            lightBg = Color(0xFFFCF8F2),
            lightSurface = Color(0xFFFFFDF9),
            lightSurfaceVariant = Color(0xFFF4ECE1),
            lightInk = Color(0xFF2E241D),
            lightSubtleInk = Color(0xFF6E5C4F),
            lightOutline = Color(0xFFE6D6C3),
            darkPrimary = Color(0xFFF5B176),
            darkOnPrimary = Color(0xFF3B1C05),
            darkPrimaryContainer = Color(0xFF542C0E),
            darkOnPrimaryContainer = Color(0xFFFFE6D1),
            darkSecondary = Color(0xFFD9B8A3),
            darkTertiary = Color(0xFF94C9A9),
            darkBg = Color(0xFF171310),
            darkSurface = Color(0xFF231D18),
            darkSurfaceVariant = Color(0xFF312821),
            darkInk = Color(0xFFFAF3EB),
            darkSubtleInk = Color(0xFFC4B3A3),
            darkOutline = Color(0xFF4A3D32)
        )

        ReturnmarkThemeMode.INDIGO_NIGHT -> PastelPaletteTokens(
            lightPrimary = Color(0xFF5C58A6),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFE6E5F7),
            lightOnPrimaryContainer = Color(0xFF1E1B4B),
            lightSecondary = Color(0xFF8A79AD),
            lightTertiary = Color(0xFFD98A5F),
            lightBg = Color(0xFFF6F6FB),
            lightSurface = Color(0xFFFCFCFF),
            lightSurfaceVariant = Color(0xFFEAEAF4),
            lightInk = Color(0xFF1D1E2E),
            lightSubtleInk = Color(0xFF575873),
            lightOutline = Color(0xFFD0D0E6),
            darkPrimary = Color(0xFFA89AF5),
            darkOnPrimary = Color(0xFF181438),
            darkPrimaryContainer = Color(0xFF2A2654),
            darkOnPrimaryContainer = Color(0xFFE8E4FF),
            darkSecondary = Color(0xFFF2B880),
            darkTertiary = Color(0xFF7FD1CC),
            darkBg = Color(0xFF111320),
            darkSurface = Color(0xFF191C2E),
            darkSurfaceVariant = Color(0xFF242840),
            darkInk = Color(0xFFF2F2FA),
            darkSubtleInk = Color(0xFFA5A8C7),
            darkOutline = Color(0xFF363C5E)
        )

        ReturnmarkThemeMode.RAINY_TOKYO -> PastelPaletteTokens(
            lightPrimary = Color(0xFF3B878C),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFDDF0F2),
            lightOnPrimaryContainer = Color(0xFF10373A),
            lightSecondary = Color(0xFF637B8C),
            lightTertiary = Color(0xFFC7738B),
            lightBg = Color(0xFFF3F7F8),
            lightSurface = Color(0xFFFBFEFF),
            lightSurfaceVariant = Color(0xFFE5EEF0),
            lightInk = Color(0xFF1A2629),
            lightSubtleInk = Color(0xFF4F6469),
            lightOutline = Color(0xFFC5D8DC),
            darkPrimary = Color(0xFF76D6D1),
            darkOnPrimary = Color(0xFF072927),
            darkPrimaryContainer = Color(0xFF153F42),
            darkOnPrimaryContainer = Color(0xFFD4F7F5),
            darkSecondary = Color(0xFFF098B3),
            darkTertiary = Color(0xFFF5C276),
            darkBg = Color(0xFF10151B),
            darkSurface = Color(0xFF171F28),
            darkSurfaceVariant = Color(0xFF212C38),
            darkInk = Color(0xFFECF4F6),
            darkSubtleInk = Color(0xFF9CB0B8),
            darkOutline = Color(0xFF314252)
        )

        ReturnmarkThemeMode.PLUM_INK -> PastelPaletteTokens(
            lightPrimary = Color(0xFF7E5285),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFF1E4F4),
            lightOnPrimaryContainer = Color(0xFF331838),
            lightSecondary = Color(0xFF9C7B9E),
            lightTertiary = Color(0xFFC7866B),
            lightBg = Color(0xFFFAF6FB),
            lightSurface = Color(0xFFFFFCFF),
            lightSurfaceVariant = Color(0xFFF1E8F3),
            lightInk = Color(0xFF281E2B),
            lightSubtleInk = Color(0xFF67566B),
            lightOutline = Color(0xFFDED0E2),
            darkPrimary = Color(0xFFD8A8E0),
            darkOnPrimary = Color(0xFF2E1233),
            darkPrimaryContainer = Color(0xFF45244B),
            darkOnPrimaryContainer = Color(0xFFF8E6FC),
            darkSecondary = Color(0xFFE8B4BC),
            darkTertiary = Color(0xFFF0BE8A),
            darkBg = Color(0xFF161218),
            darkSurface = Color(0xFF211B24),
            darkSurfaceVariant = Color(0xFF2F2633),
            darkInk = Color(0xFFF7F0F9),
            darkSubtleInk = Color(0xFFBDA8C2),
            darkOutline = Color(0xFF47394D)
        )

        ReturnmarkThemeMode.BAMBOO_GREEN -> PastelPaletteTokens(
            lightPrimary = Color(0xFF497D5E),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFDCEEE3),
            lightOnPrimaryContainer = Color(0xFF133320),
            lightSecondary = Color(0xFF8C775E),
            lightTertiary = Color(0xFF5A8A96),
            lightBg = Color(0xFFF5F8F4),
            lightSurface = Color(0xFFFCFDFB),
            lightSurfaceVariant = Color(0xFFE7EFE8),
            lightInk = Color(0xFF1E2922),
            lightSubtleInk = Color(0xFF53665A),
            lightOutline = Color(0xFFCAD9CE),
            darkPrimary = Color(0xFF88C9A1),
            darkOnPrimary = Color(0xFF0A2917),
            darkPrimaryContainer = Color(0xFF1B3F2B),
            darkOnPrimaryContainer = Color(0xFFD6F5E1),
            darkSecondary = Color(0xFFD6BA98),
            darkTertiary = Color(0xFF89C4D1),
            darkBg = Color(0xFF101612),
            darkSurface = Color(0xFF18211B),
            darkSurfaceVariant = Color(0xFF222E27),
            darkInk = Color(0xFFEDF5EF),
            darkSubtleInk = Color(0xFF9FB5A7),
            darkOutline = Color(0xFF33453A)
        )

        ReturnmarkThemeMode.SOFT_CREAM -> PastelPaletteTokens(
            lightPrimary = Color(0xFF8A6547),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFF4E8DC),
            lightOnPrimaryContainer = Color(0xFF382312),
            lightSecondary = Color(0xFF6E8273),
            lightTertiary = Color(0xFFB86B77),
            lightBg = Color(0xFFFAF6EE),
            lightSurface = Color(0xFFFFFDF8),
            lightSurfaceVariant = Color(0xFFF0E9DC),
            lightInk = Color(0xFF2B251E),
            lightSubtleInk = Color(0xFF695E52),
            lightOutline = Color(0xFFE0D5C3),
            darkPrimary = Color(0xFFE0B896),
            darkOnPrimary = Color(0xFF2E1E10),
            darkPrimaryContainer = Color(0xFF47321E),
            darkOnPrimaryContainer = Color(0xFFFAE8D7),
            darkSecondary = Color(0xFFA3C2AD),
            darkTertiary = Color(0xFFE8A2AD),
            darkBg = Color(0xFF161411),
            darkSurface = Color(0xFF211E1A),
            darkSurfaceVariant = Color(0xFF2E2A24),
            darkInk = Color(0xFFF7F2E8),
            darkSubtleInk = Color(0xFFBFB4A4),
            darkOutline = Color(0xFF453E35)
        )

        ReturnmarkThemeMode.MOONLIGHT_BLUE -> PastelPaletteTokens(
            lightPrimary = Color(0xFF4B6FA8),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFE1EAF7),
            lightOnPrimaryContainer = Color(0xFF15294A),
            lightSecondary = Color(0xFF7A7FA6),
            lightTertiary = Color(0xFF58969E),
            lightBg = Color(0xFFF4F7FB),
            lightSurface = Color(0xFFFCFDFF),
            lightSurfaceVariant = Color(0xFFE7EDF6),
            lightInk = Color(0xFF1B2330),
            lightSubtleInk = Color(0xFF536075),
            lightOutline = Color(0xFFCBD5E6),
            darkPrimary = Color(0xFF8FB5F0),
            darkOnPrimary = Color(0xFF0E1E38),
            darkPrimaryContainer = Color(0xFF1F3457),
            darkOnPrimaryContainer = Color(0xFFDCE8FF),
            darkSecondary = Color(0xFFB8BCE6),
            darkTertiary = Color(0xFF80CFD6),
            darkBg = Color(0xFF0F141E),
            darkSurface = Color(0xFF171E2B),
            darkSurfaceVariant = Color(0xFF212B3D),
            darkInk = Color(0xFFEEF3FA),
            darkSubtleInk = Color(0xFFA0AFC7),
            darkOutline = Color(0xFF324159)
        )

        ReturnmarkThemeMode.LAVENDER -> PastelPaletteTokens(
            lightPrimary = Color(0xFF7C5CE6),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFEDE7FF),
            lightOnPrimaryContainer = Color(0xFF2D196B),
            lightSecondary = Color(0xFF9F85F0),
            lightTertiary = Color(0xFFEC6B9E),
            lightBg = Color(0xFFF8F6FF),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFF0ECFA),
            lightInk = Color(0xFF1E1A2D),
            lightSubtleInk = Color(0xFF5D5675),
            lightOutline = Color(0xFFDAD2F2),
            darkPrimary = Color(0xFFBBA4FF),
            darkOnPrimary = Color(0xFF1D1042),
            darkPrimaryContainer = Color(0xFF2E2356),
            darkOnPrimaryContainer = Color(0xFFE5DCFF),
            darkSecondary = Color(0xFF94C5FF),
            darkTertiary = Color(0xFFF49AC2),
            darkBg = Color(0xFF12101B),
            darkSurface = Color(0xFF1A1726),
            darkSurfaceVariant = Color(0xFF252136),
            darkInk = Color(0xFFF5F2FF),
            darkSubtleInk = Color(0xFFB3ABC9),
            darkOutline = Color(0xFF393352)
        )

        ReturnmarkThemeMode.SKY_BLUE -> PastelPaletteTokens(
            lightPrimary = Color(0xFF2B8AD9),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFDFF1FF),
            lightOnPrimaryContainer = Color(0xFF0D3A61),
            lightSecondary = Color(0xFF45B3E0),
            lightTertiary = Color(0xFF7E6CE3),
            lightBg = Color(0xFFF3FAFF),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFE6F3FC),
            lightInk = Color(0xFF122333),
            lightSubtleInk = Color(0xFF4D667D),
            lightOutline = Color(0xFFC5E0F5),
            darkPrimary = Color(0xFF82CAFF),
            darkOnPrimary = Color(0xFF062640),
            darkPrimaryContainer = Color(0xFF153759),
            darkOnPrimaryContainer = Color(0xFFD6EEFF),
            darkSecondary = Color(0xFF67E8F9),
            darkTertiary = Color(0xFFB4A5FF),
            darkBg = Color(0xFF0E151D),
            darkSurface = Color(0xFF151F2B),
            darkSurfaceVariant = Color(0xFF1E2C3D),
            darkInk = Color(0xFFF0F8FF),
            darkSubtleInk = Color(0xFF9EB6CC),
            darkOutline = Color(0xFF2D4159)
        )

        ReturnmarkThemeMode.MINT -> PastelPaletteTokens(
            lightPrimary = Color(0xFF229E6B),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFD9F9EA),
            lightOnPrimaryContainer = Color(0xFF0A422B),
            lightSecondary = Color(0xFF34B3A0),
            lightTertiary = Color(0xFF3B82F6),
            lightBg = Color(0xFFF2FCF7),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFE3F6ED),
            lightInk = Color(0xFF122920),
            lightSubtleInk = Color(0xFF4A6B5D),
            lightOutline = Color(0xFFBEE8D4),
            darkPrimary = Color(0xFF75EFA8),
            darkOnPrimary = Color(0xFF062919),
            darkPrimaryContainer = Color(0xFF13422D),
            darkOnPrimaryContainer = Color(0xFFD1FAE5),
            darkSecondary = Color(0xFF5EEAD4),
            darkTertiary = Color(0xFF93C5FD),
            darkBg = Color(0xFF0D1713),
            darkSurface = Color(0xFF14231D),
            darkSurfaceVariant = Color(0xFF1C322A),
            darkInk = Color(0xFFECFDF5),
            darkSubtleInk = Color(0xFF9CC2B1),
            darkOutline = Color(0xFF2B4A3E)
        )

        ReturnmarkThemeMode.PEACH -> PastelPaletteTokens(
            lightPrimary = Color(0xFFE36B42),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFFFE8DF),
            lightOnPrimaryContainer = Color(0xFF591F0B),
            lightSecondary = Color(0xFFF2994A),
            lightTertiary = Color(0xFFD9468F),
            lightBg = Color(0xFFFFF8F5),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFFCEDE6),
            lightInk = Color(0xFF2B1A14),
            lightSubtleInk = Color(0xFF73564C),
            lightOutline = Color(0xFFF5D0C1),
            darkPrimary = Color(0xFFFFAE91),
            darkOnPrimary = Color(0xFF3D1405),
            darkPrimaryContainer = Color(0xFF572412),
            darkOnPrimaryContainer = Color(0xFFFFE4DB),
            darkSecondary = Color(0xFFFCD34D),
            darkTertiary = Color(0xFFF472B6),
            darkBg = Color(0xFF18110E),
            darkSurface = Color(0xFF241A16),
            darkSurfaceVariant = Color(0xFF33241F),
            darkInk = Color(0xFFFFF5F0),
            darkSubtleInk = Color(0xFFC9ABA1),
            darkOutline = Color(0xFF4D372F)
        )

        ReturnmarkThemeMode.ROSE -> PastelPaletteTokens(
            lightPrimary = Color(0xFFD64B7A),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFFFE3ED),
            lightOnPrimaryContainer = Color(0xFF57122B),
            lightSecondary = Color(0xFFAA63E3),
            lightTertiary = Color(0xFFF2845C),
            lightBg = Color(0xFFFFF5F8),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFFAE6EE),
            lightInk = Color(0xFF2B151E),
            lightSubtleInk = Color(0xFF734F5D),
            lightOutline = Color(0xFFF2C7D7),
            darkPrimary = Color(0xFFFF9BBF),
            darkOnPrimary = Color(0xFF3D0A1E),
            darkPrimaryContainer = Color(0xFF54182F),
            darkOnPrimaryContainer = Color(0xFFFFE0EC),
            darkSecondary = Color(0xFFD8B4FE),
            darkTertiary = Color(0xFFFDBA74),
            darkBg = Color(0xFF171014),
            darkSurface = Color(0xFF23181E),
            darkSurfaceVariant = Color(0xFF33222B),
            darkInk = Color(0xFFFFF0F5),
            darkSubtleInk = Color(0xFFC9A3B2),
            darkOutline = Color(0xFF4D3341)
        )

        ReturnmarkThemeMode.OCEAN -> PastelPaletteTokens(
            lightPrimary = Color(0xFF158FA6),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFD9F6FA),
            lightOnPrimaryContainer = Color(0xFF073B46),
            lightSecondary = Color(0xFF2B9AE8),
            lightTertiary = Color(0xFF2BB67D),
            lightBg = Color(0xFFF1FAFC),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFE1F3F7),
            lightInk = Color(0xFF10262B),
            lightSubtleInk = Color(0xFF496970),
            lightOutline = Color(0xFFBBE3EC),
            darkPrimary = Color(0xFF67E8F9),
            darkOnPrimary = Color(0xFF052B33),
            darkPrimaryContainer = Color(0xFF10424D),
            darkOnPrimaryContainer = Color(0xFFCFFAFE),
            darkSecondary = Color(0xFF7DD3FC),
            darkTertiary = Color(0xFF6EE7B7),
            darkBg = Color(0xFF0C1619),
            darkSurface = Color(0xFF132227),
            darkSurfaceVariant = Color(0xFF1B3138),
            darkInk = Color(0xFFECFEFF),
            darkSubtleInk = Color(0xFF98BDC4),
            darkOutline = Color(0xFF294852)
        )

        ReturnmarkThemeMode.MIDNIGHT -> PastelPaletteTokens(
            lightPrimary = Color(0xFF5A54D1),
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFE6E6FF),
            lightOnPrimaryContainer = Color(0xFF1B175E),
            lightSecondary = Color(0xFF2DA4C7),
            lightTertiary = Color(0xFF22A06B),
            lightBg = Color(0xFFF4F5FA),
            lightSurface = Color(0xFFFFFFFF),
            lightSurfaceVariant = Color(0xFFE7E9F5),
            lightInk = Color(0xFF151826),
            lightSubtleInk = Color(0xFF525870),
            lightOutline = Color(0xFFCFD3E8),
            darkPrimary = Color(0xFFA78BFA),
            darkOnPrimary = Color(0xFF15102E),
            darkPrimaryContainer = Color(0xFF28224A),
            darkOnPrimaryContainer = Color(0xFFEDE9FE),
            darkSecondary = Color(0xFF67E8F9),
            darkTertiary = Color(0xFF6EE7B7),
            darkBg = Color(0xFF0E111A),
            darkSurface = Color(0xFF161A27),
            darkSurfaceVariant = Color(0xFF202638),
            darkInk = Color(0xFFF3F4F9),
            darkSubtleInk = Color(0xFFA0A8C0),
            darkOutline = Color(0xFF313954)
        )

        else -> PastelPaletteTokens(
            lightPrimary = ForestInk,
            lightOnPrimary = Color.White,
            lightPrimaryContainer = Color(0xFFD8F3DC),
            lightOnPrimaryContainer = ForestInk,
            lightSecondary = Color(0xFF2D6A4F),
            lightTertiary = Color(0xFF1D4ED8),
            lightBg = IvoryBg,
            lightSurface = IvorySurface,
            lightSurfaceVariant = IvorySurfaceVariant,
            lightInk = CharcoalInk,
            lightSubtleInk = WarmGrayText,
            lightOutline = Color(0xFFD6CFC2),
            darkPrimary = ElectricLime,
            darkOnPrimary = Color(0xFF0E1013),
            darkPrimaryContainer = Color(0xFF242B18),
            darkOnPrimaryContainer = ElectricLime,
            darkSecondary = MintEmerald,
            darkTertiary = Color(0xFF60A5FA),
            darkBg = MidnightBg,
            darkSurface = MidnightSurface,
            darkSurfaceVariant = MidnightSurfaceVariant,
            darkInk = OffWhiteInk,
            darkSubtleInk = MutedSlateText,
            darkOutline = Color(0xFF2E3543)
        )
    }
}

fun returnmarkSoftGradientBrush(primary: Color, secondary: Color, isDark: Boolean): Brush {
    val alphaA = if (isDark) 0.24f else 0.18f
    val alphaB = if (isDark) 0.10f else 0.08f
    return Brush.linearGradient(
        colors = listOf(
            primary.copy(alpha = alphaA),
            secondary.copy(alpha = alphaB)
        )
    )
}

fun parseHexColorSafely(hex: String, fallback: Color = ElectricLime): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val value = when (clean.length) {
            6 -> ("FF$clean").toLong(16)
            8 -> clean.toLong(16)
            else -> return fallback
        }
        Color(value)
    } catch (_: Exception) {
        fallback
    }
}
