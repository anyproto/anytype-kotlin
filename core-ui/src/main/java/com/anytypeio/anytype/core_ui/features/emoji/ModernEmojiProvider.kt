package com.anytypeio.anytype.core_ui.features.emoji

import android.content.Context
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

/**
 * Modern emoji provider using EmojiCompat2 for native emoji rendering.
 * This implementation reduces APK size and provides automatic support for new emojis.
 */
interface ModernEmojiProvider {
    /**
     * Initialize the emoji provider
     */
    suspend fun initialize(): Boolean
    
    /**
     * Check if an emoji is supported by the system
     */
    fun isEmojiSupported(emoji: String): Boolean
    
    /**
     * Process text to replace emoji unicode with EmojiSpans
     */
    fun process(text: CharSequence): CharSequence
    
    /**
     * Get the rendering mode
     */
    fun getRenderingMode(): EmojiRenderingMode
}

enum class EmojiRenderingMode {
    /**
     * Use native system emoji rendering (fastest, smallest APK)
     */
    NATIVE,
    
    /**
     * Use EmojiCompat2 with bundled fonts (consistent across devices)
     */
    EMOJI_COMPAT_BUNDLED,
    
    /**
     * Use EmojiCompat2 with downloadable fonts (latest emojis, requires internet)
     */
    EMOJI_COMPAT_DOWNLOADABLE,
    
    /**
     * Legacy mode using PNG assets (fallback for compatibility)
     */
    LEGACY_PNG
}

class ModernEmojiProviderImpl(
    private val context: Context,
    private val renderingMode: EmojiRenderingMode = EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
) : ModernEmojiProvider {
    
    private var isInitialized = false
    
    override suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        when (renderingMode) {
            EmojiRenderingMode.NATIVE -> {
                // Native rendering doesn't need initialization
                isInitialized = true
                continuation.resume(true)
            }
            
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED -> {
                try {
                    val config = BundledEmojiCompatConfig(context)
                        .setReplaceAll(true)
                        .setEmojiSpanIndicatorEnabled(false)
                        .registerInitCallback(object : EmojiCompat.InitCallback() {
                            override fun onInitialized() {
                                Timber.d("EmojiCompat initialized successfully")
                                isInitialized = true
                                if (continuation.isActive) {
                                    continuation.resume(true)
                                }
                            }
                            
                            override fun onFailed(throwable: Throwable?) {
                                Timber.e(throwable, "EmojiCompat initialization failed")
                                isInitialized = false
                                if (continuation.isActive) {
                                    continuation.resume(false)
                                }
                            }
                        })
                    
                    EmojiCompat.init(config)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to initialize EmojiCompat")
                    continuation.resume(false)
                }
            }
            
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> {
                // TODO: Implement downloadable fonts support
                Timber.w("Downloadable fonts not yet implemented, falling back to bundled")
                isInitialized = false
                continuation.resume(false)
            }
            
            EmojiRenderingMode.LEGACY_PNG -> {
                // Legacy mode doesn't need special initialization
                isInitialized = true
                continuation.resume(true)
            }
        }
    }
    
    override fun isEmojiSupported(emoji: String): Boolean {
        if (!isInitialized) return false
        
        return when (renderingMode) {
            EmojiRenderingMode.NATIVE -> true // Assume system supports it
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED,
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> {
                try {
                    EmojiCompat.get().hasEmojiGlyph(emoji)
                } catch (e: Exception) {
                    Timber.w(e, "Failed to check emoji support")
                    false
                }
            }
            EmojiRenderingMode.LEGACY_PNG -> false // Let legacy handler deal with it
        }
    }
    
    override fun process(text: CharSequence): CharSequence {
        if (!isInitialized) return text
        
        return when (renderingMode) {
            EmojiRenderingMode.NATIVE -> text
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED,
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> {
                try {
                    EmojiCompat.get().process(text) ?: text
                } catch (e: Exception) {
                    Timber.w(e, "Failed to process text with EmojiCompat")
                    text
                }
            }
            EmojiRenderingMode.LEGACY_PNG -> text
        }
    }
    
    override fun getRenderingMode(): EmojiRenderingMode = renderingMode
}