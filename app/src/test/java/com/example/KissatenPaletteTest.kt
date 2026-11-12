package com.example

import androidx.compose.ui.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.theme.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class KissatenPaletteTest {
    @Test fun everyPaletteAndAppearanceHasReadableInkAndForcedOledStaysBlack() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        for (palette in ReturnmarkThemeMode.entries) for (mode in ThemeAppearanceMode.entries) for (systemDark in listOf(false,true)) {
            val colors=resolveReturnmarkColors(context,palette,mode,palette.previewAccentHex,systemDark)
            assertTrue("$palette/$mode primary",kissatenContrast(colors.primary,colors.surface)>=4.5f)
            assertTrue("$palette/$mode ink",kissatenContrast(colors.onSurface,colors.surface)>=4.5f)
            assertTrue("$palette/$mode button",kissatenContrast(colors.primary,colors.onPrimary)>=4.5f)
            if (mode==ThemeAppearanceMode.OLED || palette==ReturnmarkThemeMode.DARK_OLED) assertEquals(Color.Black,colors.background)
        }
    }
}
