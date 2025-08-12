package com.anytypeio.anytype.core_ui.features.emoji

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.objectIcon.EmojiIconView
import com.anytypeio.anytype.core_ui.widgets.objectIcon.ModernEmojiIconView
import com.anytypeio.anytype.presentation.objects.ObjectIcon

/**
 * Unified emoji renderer that can switch between legacy and modern implementations.
 * This abstraction allows for gradual migration while maintaining compatibility.
 */
object EmojiRenderer {
    
    /**
     * Configuration for emoji rendering
     */
    data class Config(
        val useModernRenderer: Boolean = false,
        val renderingMode: EmojiRenderingMode = EmojiRenderingMode.EMOJI_COMPAT_BUNDLED,
        val enableFallback: Boolean = true
    )
    
    /**
     * Default configuration - can be overridden via DI or settings
     */
    private var defaultConfig = Config()
    
    /**
     * Update the default configuration
     */
    fun setDefaultConfig(config: Config) {
        defaultConfig = config
    }
    
    /**
     * Render an emoji icon using the configured renderer
     */
    @Composable
    fun EmojiIcon(
        modifier: Modifier = Modifier,
        icon: ObjectIcon.Basic.Emoji,
        backgroundSize: Dp,
        iconWithoutBackgroundMaxSize: Dp,
        backgroundColor: Int = R.color.shape_tertiary,
        config: Config? = null
    ) {
        val effectiveConfig = config ?: defaultConfig
        
        if (effectiveConfig.useModernRenderer) {
            ModernEmojiIconView(
                modifier = modifier,
                icon = icon,
                backgroundSize = backgroundSize,
                iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize,
                backgroundColor = backgroundColor,
                renderingMode = effectiveConfig.renderingMode
            )
        } else {
            // Use legacy implementation
            EmojiIconView(
                modifier = modifier,
                icon = icon,
                backgroundSize = backgroundSize,
                iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize,
                backgroundColor = backgroundColor
            )
        }
    }
}

/**
 * Feature flags for emoji rendering
 */
object EmojiFeatureFlags {
    
    /**
     * Check if modern emoji rendering should be enabled
     * This can be controlled via remote config, build variants, or user preferences
     */
    fun isModernEmojiEnabled(): Boolean {
        // For now, return false to maintain compatibility
        // In the future, this could be controlled by:
        // - Build variant (enable in debug/beta)
        // - Remote config flag
        // - User preference
        // - Device capabilities
        return false
    }
    
    /**
     * Get preferred rendering mode based on device capabilities and settings
     */
    fun getPreferredRenderingMode(): EmojiRenderingMode {
        // Logic to determine best rendering mode:
        // - Check Android version
        // - Check available system fonts
        // - Consider user preferences
        // - Fall back to bundled if needed
        
        return when {
            android.os.Build.VERSION.SDK_INT >= 28 -> {
                // Android 9+ has better emoji support
                EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
            }
            android.os.Build.VERSION.SDK_INT >= 23 -> {
                // Android 6+ can use EmojiCompat
                EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
            }
            else -> {
                // Older versions fall back to PNG assets
                EmojiRenderingMode.LEGACY_PNG
            }
        }
    }
}