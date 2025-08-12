package com.anytypeio.anytype.core_ui.features.emoji

import android.content.Context
import android.content.SharedPreferences
import com.anytypeio.anytype.core_ui.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Configuration manager for emoji rendering preferences.
 * Allows users and the app to control emoji rendering behavior.
 */

class EmojiConfiguration(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "emoji_preferences"
        private const val KEY_MODERN_EMOJI_ENABLED = "modern_emoji_enabled"
        private const val KEY_RENDERING_MODE = "rendering_mode"
        private const val KEY_AUTO_FALLBACK_ENABLED = "auto_fallback_enabled"
        private const val KEY_PERFORMANCE_MODE = "performance_mode"
        private const val KEY_ACCESSIBILITY_MODE = "accessibility_mode"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _configurationFlow = MutableStateFlow(getCurrentConfiguration())
    
    /**
     * Flow of current emoji configuration
     */
    val configurationFlow: Flow<Configuration> = _configurationFlow.asStateFlow()
    
    /**
     * Get current configuration
     */
    fun getCurrentConfiguration(): Configuration {
        return Configuration(
            modernEmojiEnabled = prefs.getBoolean(KEY_MODERN_EMOJI_ENABLED, false),
            renderingMode = parseRenderingMode(prefs.getString(KEY_RENDERING_MODE, null)),
            autoFallbackEnabled = prefs.getBoolean(KEY_AUTO_FALLBACK_ENABLED, true),
            performanceMode = prefs.getBoolean(KEY_PERFORMANCE_MODE, false),
            accessibilityMode = prefs.getBoolean(KEY_ACCESSIBILITY_MODE, false)
        )
    }
    
    /**
     * Enable or disable modern emoji rendering
     */
    fun setModernEmojiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MODERN_EMOJI_ENABLED, enabled).apply()
        updateFlow()
    }
    
    /**
     * Set preferred rendering mode
     */
    fun setRenderingMode(mode: EmojiRenderingMode) {
        prefs.edit().putString(KEY_RENDERING_MODE, mode.name).apply()
        updateFlow()
    }
    
    /**
     * Set auto-fallback preference
     */
    fun setAutoFallbackEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_FALLBACK_ENABLED, enabled).apply()
        updateFlow()
    }
    
    /**
     * Enable performance mode (prioritizes speed over quality)
     */
    fun setPerformanceMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PERFORMANCE_MODE, enabled).apply()
        updateFlow()
    }
    
    /**
     * Enable accessibility mode (ensures maximum compatibility)
     */
    fun setAccessibilityMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ACCESSIBILITY_MODE, enabled).apply()
        updateFlow()
    }
    
    /**
     * Reset all preferences to defaults
     */
    fun resetToDefaults() {
        prefs.edit().clear().apply()
        updateFlow()
    }
    
    private fun updateFlow() {
        _configurationFlow.value = getCurrentConfiguration()
    }
    
    private fun parseRenderingMode(value: String?): EmojiRenderingMode {
        return try {
            value?.let { EmojiRenderingMode.valueOf(it) } ?: getDefaultRenderingMode()
        } catch (e: IllegalArgumentException) {
            getDefaultRenderingMode()
        }
    }
    
    private fun getDefaultRenderingMode(): EmojiRenderingMode {
        return when {
            android.os.Build.VERSION.SDK_INT >= 28 -> EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
            android.os.Build.VERSION.SDK_INT >= 23 -> EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
            else -> EmojiRenderingMode.LEGACY_PNG
        }
    }
    
    data class Configuration(
        val modernEmojiEnabled: Boolean,
        val renderingMode: EmojiRenderingMode,
        val autoFallbackEnabled: Boolean,
        val performanceMode: Boolean,
        val accessibilityMode: Boolean
    ) {
        
        /**
         * Get the effective rendering mode considering all settings
         */
        fun getEffectiveRenderingMode(): EmojiRenderingMode {
            return when {
                !modernEmojiEnabled -> EmojiRenderingMode.LEGACY_PNG
                accessibilityMode -> EmojiRenderingMode.LEGACY_PNG // Most compatible
                performanceMode -> EmojiRenderingMode.NATIVE // Fastest
                else -> renderingMode
            }
        }
        
        /**
         * Convert to EmojiRenderer.Config
         */
        fun toRendererConfig(): EmojiRenderer.Config {
            return EmojiRenderer.Config(
                useModernRenderer = modernEmojiEnabled,
                renderingMode = getEffectiveRenderingMode(),
                enableFallback = autoFallbackEnabled
            )
        }
    }
}

/**
 * Emoji feature configuration that can be controlled remotely or via build variants
 */
object EmojiFeatureConfig {
    
    /**
     * Whether modern emoji features are available in this build
     */
    fun isModernEmojiAvailable(): Boolean {
        // Could be controlled by:
        // - Build variant (enable in debug/beta)
        // - Feature flags
        // - Remote config
        return true // For now, always available
    }
    
    /**
     * Whether to show emoji rendering options in settings
     */
    fun showEmojiSettings(): Boolean {
        return isModernEmojiAvailable() && 
               (BuildConfig.DEBUG || isEmojiExperimentEnabled())
    }
    
    /**
     * Check if emoji experiments are enabled
     */
    private fun isEmojiExperimentEnabled(): Boolean {
        // This could check:
        // - Remote config flags
        // - User is in beta group
        // - Device is registered for experiments
        return false
    }
    
    /**
     * Get recommended configuration for current build/user
     */
    fun getRecommendedConfiguration(): EmojiConfiguration.Configuration {
        return EmojiConfiguration.Configuration(
            modernEmojiEnabled = when {
                BuildConfig.DEBUG -> true // Enable in debug builds
                isEmojiExperimentEnabled() -> true // Enable for beta users
                else -> false // Disabled in production for now
            },
            renderingMode = EmojiRenderingMode.EMOJI_COMPAT_BUNDLED,
            autoFallbackEnabled = true,
            performanceMode = false,
            accessibilityMode = false
        )
    }
}