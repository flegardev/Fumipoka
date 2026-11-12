package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.returnmarkDataStore by preferencesDataStore(name = "returnmark_preferences_v2")

enum class ThemeAppearanceMode(val label: String, val shortBadge: String) {
    LIGHT("Washi Light", "Light"),
    DARK("Rainy Lofi Dark", "Dark"),
    OLED("True OLED", "OLED"),
    SYSTEM("System Auto", "System")
}

enum class ReturnmarkThemeMode(
    val label: String,
    val subtitle: String,
    val previewBgHex: String,
    val previewAccentHex: String,
    val isDark: Boolean,
    val isPastelSignature: Boolean = false,
    val isLofiJapanese: Boolean = false
) {
    // Core Lofi Whimsical Japanese & Kyoto Café Palettes (100% Free)
    SAKURA_BLUSH("Sakura Blush", "Muted sakura petals, warm washi cream & gentle plum ink", "#FBF5F5", "#C86D85", false, true, true),
    MATCHA_MIST("Matcha Mist", "Whisked uji matcha, tatami sage & morning mist paper", "#F4F7F2", "#598B6E", false, true, true),
    KYOTO_SKY("Kyoto Sky", "Faded Kamo river sky blue, paper clouds & twilight slate", "#F3F7FA", "#5386B6", false, true, true),
    LANTERN_AMBER("Lantern Amber", "Warm paper lantern glow, roasted cedar wood & café cream", "#FBF6EE", "#C97A3E", false, true, true),
    INDIGO_NIGHT("Indigo Night", "Rainy-night Kyoto indigo, dusty lavender & warm window light", "#131624", "#9E8CE8", true, true, true),
    RAINY_TOKYO("Rainy Tokyo", "Charcoal blue-gray alleyways, muted teal & soft neon reflection", "#121820", "#5CC8C2", true, true, true),
    PLUM_INK("Plum Ink", "Fountain-pen plum ink, dusty wisteria lavender & notebook cream", "#F8F4F9", "#7E5285", false, true, true),
    BAMBOO_GREEN("Bamboo Green", "Arashiyama bamboo grove, mossy stone & warm hinoki wood", "#F3F7F3", "#497D5E", false, true, true),
    SOFT_CREAM("Soft Cream", "Vintage Japanese stationery, warm beige washi & sumi brush ink", "#FAF6EE", "#8A6547", false, true, true),
    MOONLIGHT_BLUE("Moonlight Blue", "Midnight train ride, moonlight blue & quiet silver mist", "#111722", "#7AA6E8", true, true, true),

    // Returnmark 3.0 Signature Soft & Bright Pastel Themes (100% Free)
    LAVENDER("Lavender", "Soft dreamy lavender & periwinkle bloom", "#F6F3FF", "#8B6FE8", false, true, false),
    SKY_BLUE("Sky Blue", "Bright airy sky & crystal azure breeze", "#F0F8FF", "#3B9AE1", false, true, false),
    MINT("Mint", "Fresh botanical mint & soft sage", "#EFFBF5", "#2BB67D", false, true, false),
    PEACH("Peach", "Warm pastel apricot & coral sunrise", "#FFF6F0", "#F07B52", false, true, false),
    ROSE("Rose", "Delicate blush rose petal & berry cream", "#FFF2F6", "#E25B88", false, true, false),
    OCEAN("Ocean", "Coastal aqua & serene turquoise lagoon", "#EEF9FB", "#1CA3BA", false, true, false),
    MIDNIGHT("Midnight", "Velvet twilight slate & pastel lavender-cyan", "#121521", "#A78BFA", true, true, false),

    // Classic & Studio Themes (Preserved for 100% backward compatibility)
    MIDNIGHT_GRAPHITE("Midnight Graphite", "Linear-inspired slate & electric lime", "#121417", "#CCFF00", true, false, false),
    SOFT_IVORY("Soft Ivory", "Warm editorial paper & forest ink", "#FAF8F5", "#1B4332", false, false, false),
    FOREST_GREEN("Forest Green", "Deep botanical pine & lime highlights", "#0D1F17", "#7CE38B", true, false, false),
    ELECTRIC_LIME("Electric Lime Studio", "High-energy carbon & volt accents", "#0F120E", "#D9FF00", true, false, false),
    DARK_OLED("True Dark OLED", "Pure #000000 black for AMOLED screens", "#000000", "#CCFF00", true, false, false),
    MATERIAL_YOU("Material You Dynamic", "Adapts to your Android wallpaper palette", "#181C20", "#8CD8FF", true, false, false),
    HIGH_CONTRAST("High Contrast Accessible", "Maximum legibility WCAG AAA contrast", "#000000", "#FFFF00", true, false, false)
}

enum class LayoutDensityMode(val label: String) {
    COMPACT("Compact"),
    SPACIOUS("Spacious")
}

data class UserPreferencesState(
    val themeMode: ReturnmarkThemeMode = ReturnmarkThemeMode.SAKURA_BLUSH,
    val appearanceMode: ThemeAppearanceMode = ThemeAppearanceMode.LIGHT,
    val customAccentHex: String = "#C86D85",
    val layoutDensity: LayoutDensityMode = LayoutDensityMode.SPACIOUS,
    val fontScale: Float = 1.0f,
    val reducedMotion: Boolean = false,
    val showStatistics: Boolean = true,
    val autoPromptSavePointAfterFocus: Boolean = true,
    val focusNotificationsEnabled: Boolean = true,
    val persistentContextNotification: Boolean = false,
    val startDestination: String = "HOME",
    val selectedProjectId: Long = 1L,
    val activeLayoutPresetName: String = "Command Center (All 20)",
    val widgetConfigsRaw: String = "",
    val savedLayoutPresetsRaw: String = "",
    val savedSearchesCsv: String = "ble,protocol,chapter-4,urgent",
    val customTemplatesRaw: String = ""
) {
    fun parsedWidgetConfigs(): List<DashboardWidgetConfig> {
        val defaults = DashboardWidgetType.entries.map {
            DashboardWidgetConfig(
                type = it,
                isVisible = true,
                size = if (it.index <= 4) WidgetSize.EXPANDED else WidgetSize.MEDIUM,
                accentHex = customAccentHex,
                orderIndex = it.index
            )
        }
        if (widgetConfigsRaw.isBlank()) return defaults
        val mapByType = widgetConfigsRaw.split("||").mapNotNull { entry ->
            val p = entry.split("::")
            val type = p.getOrNull(0)?.let { name ->
                DashboardWidgetType.entries.find { it.name == name }
            } ?: return@mapNotNull null
            type to DashboardWidgetConfig(
                type = type,
                isVisible = p.getOrNull(1)?.toBoolean() ?: true,
                size = p.getOrNull(2)?.let { s -> WidgetSize.entries.find { it.name == s } } ?: WidgetSize.MEDIUM,
                accentHex = p.getOrNull(3) ?: customAccentHex,
                orderIndex = p.getOrNull(4)?.toIntOrNull() ?: type.index
            )
        }.toMap()

        return DashboardWidgetType.entries.map { type ->
            mapByType[type] ?: DashboardWidgetConfig(type = type, orderIndex = type.index)
        }.sortedBy { it.orderIndex }
    }

    companion object {
        fun encodeWidgetConfigs(configs: List<DashboardWidgetConfig>): String {
            return configs.joinToString("||") {
                "${it.type.name}::${it.isVisible}::${it.size.name}::${it.accentHex}::${it.orderIndex}"
            }
        }
    }
}

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        val CUSTOM_ACCENT = stringPreferencesKey("custom_accent_hex")
        val LAYOUT_DENSITY = stringPreferencesKey("layout_density")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val SHOW_STATS = booleanPreferencesKey("show_statistics")
        val AUTO_PROMPT_SP = booleanPreferencesKey("auto_prompt_save_point")
        val FOCUS_NOTIFICATIONS = booleanPreferencesKey("focus_notifications_enabled")
        val PERSISTENT_CONTEXT_NOTIF = booleanPreferencesKey("persistent_context_notification")
        val START_DEST = stringPreferencesKey("start_destination")
        val SELECTED_PROJECT_ID = longPreferencesKey("selected_project_id")
        val ACTIVE_PRESET_NAME = stringPreferencesKey("active_layout_preset_name")
        val WIDGET_CONFIGS = stringPreferencesKey("widget_configs_raw")
        val SAVED_PRESETS = stringPreferencesKey("saved_layout_presets_raw")
        val SAVED_SEARCHES = stringPreferencesKey("saved_searches_csv")
        val CUSTOM_TEMPLATES = stringPreferencesKey("custom_templates_raw")
    }

    val preferencesFlow: Flow<UserPreferencesState> = context.returnmarkDataStore.data.map { prefs ->
        val resolvedTheme = prefs[Keys.THEME_MODE]?.let { name ->
            ReturnmarkThemeMode.entries.find { it.name == name }
        } ?: ReturnmarkThemeMode.SAKURA_BLUSH

        val resolvedAppearance = prefs[Keys.APPEARANCE_MODE]?.let { name ->
            ThemeAppearanceMode.entries.find { it.name == name }
        } ?: when {
            resolvedTheme == ReturnmarkThemeMode.DARK_OLED -> ThemeAppearanceMode.OLED
            resolvedTheme.isDark -> ThemeAppearanceMode.DARK
            else -> ThemeAppearanceMode.LIGHT
        }

        UserPreferencesState(
            themeMode = resolvedTheme,
            appearanceMode = resolvedAppearance,
            customAccentHex = prefs[Keys.CUSTOM_ACCENT] ?: resolvedTheme.previewAccentHex,
            layoutDensity = prefs[Keys.LAYOUT_DENSITY]?.let { name ->
                LayoutDensityMode.entries.find { it.name == name }
            } ?: LayoutDensityMode.SPACIOUS,
            fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            showStatistics = prefs[Keys.SHOW_STATS] ?: true,
            autoPromptSavePointAfterFocus = prefs[Keys.AUTO_PROMPT_SP] ?: true,
            focusNotificationsEnabled = prefs[Keys.FOCUS_NOTIFICATIONS] ?: true,
            persistentContextNotification = prefs[Keys.PERSISTENT_CONTEXT_NOTIF] ?: false,
            startDestination = prefs[Keys.START_DEST] ?: "HOME",
            selectedProjectId = prefs[Keys.SELECTED_PROJECT_ID] ?: 1L,
            activeLayoutPresetName = prefs[Keys.ACTIVE_PRESET_NAME] ?: "Command Center (All 20)",
            widgetConfigsRaw = prefs[Keys.WIDGET_CONFIGS] ?: "",
            savedLayoutPresetsRaw = prefs[Keys.SAVED_PRESETS] ?: "",
            savedSearchesCsv = prefs[Keys.SAVED_SEARCHES] ?: "ble,protocol,chapter-4,urgent",
            customTemplatesRaw = prefs[Keys.CUSTOM_TEMPLATES] ?: ""
        )
    }

    suspend fun getSnapshot(): UserPreferencesState = preferencesFlow.first()

    suspend fun setThemeMode(mode: ReturnmarkThemeMode) {
        context.returnmarkDataStore.edit {
            it[Keys.THEME_MODE] = mode.name
            it[Keys.CUSTOM_ACCENT] = mode.previewAccentHex
            if (mode == ReturnmarkThemeMode.DARK_OLED) {
                it[Keys.APPEARANCE_MODE] = ThemeAppearanceMode.OLED.name
            } else if (mode.isDark && it[Keys.APPEARANCE_MODE] == null) {
                it[Keys.APPEARANCE_MODE] = ThemeAppearanceMode.DARK.name
            } else if (!mode.isDark && it[Keys.APPEARANCE_MODE] == null) {
                it[Keys.APPEARANCE_MODE] = ThemeAppearanceMode.LIGHT.name
            }
        }
    }

    suspend fun setAppearanceMode(mode: ThemeAppearanceMode) {
        context.returnmarkDataStore.edit {
            it[Keys.APPEARANCE_MODE] = mode.name
        }
    }

    suspend fun cycleAppearanceMode() {
        context.returnmarkDataStore.edit { prefs ->
            val current = prefs[Keys.APPEARANCE_MODE]?.let { name ->
                ThemeAppearanceMode.entries.find { it.name == name }
            } ?: ThemeAppearanceMode.LIGHT
            val next = when (current) {
                ThemeAppearanceMode.LIGHT -> ThemeAppearanceMode.DARK
                ThemeAppearanceMode.DARK -> ThemeAppearanceMode.OLED
                ThemeAppearanceMode.OLED -> ThemeAppearanceMode.SYSTEM
                ThemeAppearanceMode.SYSTEM -> ThemeAppearanceMode.LIGHT
            }
            prefs[Keys.APPEARANCE_MODE] = next.name
        }
    }

    suspend fun setCustomAccent(hex: String) {
        context.returnmarkDataStore.edit { it[Keys.CUSTOM_ACCENT] = hex }
    }

    suspend fun setLayoutDensity(density: LayoutDensityMode) {
        context.returnmarkDataStore.edit { it[Keys.LAYOUT_DENSITY] = density.name }
    }

    suspend fun setFontScale(scale: Float) {
        context.returnmarkDataStore.edit { it[Keys.FONT_SCALE] = scale.coerceIn(0.85f, 1.35f) }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.returnmarkDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setShowStatistics(enabled: Boolean) {
        context.returnmarkDataStore.edit { it[Keys.SHOW_STATS] = enabled }
    }

    suspend fun setAutoPromptSavePoint(enabled: Boolean) {
        context.returnmarkDataStore.edit { it[Keys.AUTO_PROMPT_SP] = enabled }
    }

    suspend fun setFocusNotificationsEnabled(enabled: Boolean) {
        context.returnmarkDataStore.edit { it[Keys.FOCUS_NOTIFICATIONS] = enabled }
    }

    suspend fun setPersistentContextNotification(enabled: Boolean) {
        context.returnmarkDataStore.edit { it[Keys.PERSISTENT_CONTEXT_NOTIF] = enabled }
    }

    suspend fun setStartDestination(dest: String) {
        context.returnmarkDataStore.edit { it[Keys.START_DEST] = dest }
    }

    suspend fun setSelectedProjectId(projectId: Long) {
        context.returnmarkDataStore.edit { it[Keys.SELECTED_PROJECT_ID] = projectId }
    }

    suspend fun updateWidgetConfigs(configs: List<DashboardWidgetConfig>, presetName: String? = null) {
        context.returnmarkDataStore.edit {
            it[Keys.WIDGET_CONFIGS] = UserPreferencesState.encodeWidgetConfigs(configs)
            if (presetName != null) {
                it[Keys.ACTIVE_PRESET_NAME] = presetName
            }
        }
    }

    suspend fun saveNamedLayoutPreset(name: String, configs: List<DashboardWidgetConfig>) {
        context.returnmarkDataStore.edit { prefs ->
            val encoded = UserPreferencesState.encodeWidgetConfigs(configs)
            val existing = prefs[Keys.SAVED_PRESETS] ?: ""
            val entries = existing.split("@@").filter { it.isNotBlank() && !it.startsWith("$name##") }.toMutableList()
            entries.add("$name##$encoded")
            prefs[Keys.SAVED_PRESETS] = entries.joinToString("@@")
            prefs[Keys.ACTIVE_PRESET_NAME] = name
            prefs[Keys.WIDGET_CONFIGS] = encoded
        }
    }

    suspend fun addSavedSearch(query: String) {
        val clean = query.trim()
        if (clean.isBlank()) return
        context.returnmarkDataStore.edit { prefs ->
            val current = (prefs[Keys.SAVED_SEARCHES] ?: "").split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
            if (!current.contains(clean)) {
                current.add(0, clean)
            }
            prefs[Keys.SAVED_SEARCHES] = current.take(12).joinToString(",")
        }
    }

    suspend fun saveCustomTemplate(name: String, tagline: String, missionsCsv: String, milestonesCsv: String) {
        context.returnmarkDataStore.edit { prefs ->
            val existing = prefs[Keys.CUSTOM_TEMPLATES] ?: ""
            val entry = "${System.currentTimeMillis()}::$name::$tagline::$missionsCsv::$milestonesCsv"
            val updated = if (existing.isBlank()) entry else "$existing||$entry"
            prefs[Keys.CUSTOM_TEMPLATES] = updated
        }
    }

    suspend fun restorePreferencesState(state: UserPreferencesState) {
        context.returnmarkDataStore.edit {
            it[Keys.THEME_MODE] = state.themeMode.name
            it[Keys.APPEARANCE_MODE] = state.appearanceMode.name
            it[Keys.CUSTOM_ACCENT] = state.customAccentHex
            it[Keys.LAYOUT_DENSITY] = state.layoutDensity.name
            it[Keys.FONT_SCALE] = state.fontScale
            it[Keys.REDUCED_MOTION] = state.reducedMotion
            it[Keys.SHOW_STATS] = state.showStatistics
            it[Keys.AUTO_PROMPT_SP] = state.autoPromptSavePointAfterFocus
            it[Keys.FOCUS_NOTIFICATIONS] = state.focusNotificationsEnabled
            it[Keys.PERSISTENT_CONTEXT_NOTIF] = state.persistentContextNotification
            it[Keys.START_DEST] = state.startDestination
            it[Keys.SELECTED_PROJECT_ID] = state.selectedProjectId
            it[Keys.ACTIVE_PRESET_NAME] = state.activeLayoutPresetName
            it[Keys.WIDGET_CONFIGS] = state.widgetConfigsRaw
            it[Keys.SAVED_PRESETS] = state.savedLayoutPresetsRaw
            it[Keys.SAVED_SEARCHES] = state.savedSearchesCsv
            it[Keys.CUSTOM_TEMPLATES] = state.customTemplatesRaw
        }
    }
}
