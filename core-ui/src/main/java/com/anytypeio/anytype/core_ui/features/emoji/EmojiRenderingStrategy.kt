package com.anytypeio.anytype.core_ui.features.emoji

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Manages emoji rendering strategy with intelligent fallback mechanisms.
 * This class determines the best rendering approach based on:
 * - Device capabilities
 * - Network availability
 * - User preferences
 * - Performance metrics
 */
class EmojiRenderingStrategy(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    
    private var currentStrategy: RenderingStrategy = RenderingStrategy.DETERMINING
    private var fallbackChain: List<EmojiRenderingMode> = emptyList()
    
    private val listeners = mutableSetOf<StrategyChangeListener>()
    
    interface StrategyChangeListener {
        fun onStrategyChanged(strategy: RenderingStrategy, mode: EmojiRenderingMode)
    }
    
    enum class RenderingStrategy {
        DETERMINING,
        OPTIMAL,
        FALLBACK,
        LEGACY
    }
    
    init {
        determineOptimalStrategy()
    }
    
    /**
     * Add a listener for strategy changes
     */
    fun addListener(listener: StrategyChangeListener) {
        listeners.add(listener)
    }
    
    /**
     * Remove a strategy change listener
     */
    fun removeListener(listener: StrategyChangeListener) {
        listeners.remove(listener)
    }
    
    /**
     * Get the current recommended rendering mode
     */
    fun getCurrentRenderingMode(): EmojiRenderingMode {
        return when (currentStrategy) {
            RenderingStrategy.DETERMINING -> EmojiRenderingMode.LEGACY_PNG
            RenderingStrategy.OPTIMAL -> determineOptimalMode()
            RenderingStrategy.FALLBACK -> getFallbackMode()
            RenderingStrategy.LEGACY -> EmojiRenderingMode.LEGACY_PNG
        }
    }
    
    /**
     * Test if a specific rendering mode works on this device
     */
    suspend fun testRenderingMode(mode: EmojiRenderingMode): Boolean {
        return try {
            val provider = ModernEmojiProviderImpl(context, mode)
            val initialized = provider.initialize()
            
            if (initialized) {
                // Test with a common emoji
                val testEmoji = "😀"
                provider.isEmojiSupported(testEmoji)
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.w(e, "Failed to test rendering mode: $mode")
            false
        }
    }
    
    /**
     * Force a specific rendering mode (for testing or user preference)
     */
    fun forceRenderingMode(mode: EmojiRenderingMode) {
        currentStrategy = when (mode) {
            EmojiRenderingMode.LEGACY_PNG -> RenderingStrategy.LEGACY
            else -> RenderingStrategy.OPTIMAL
        }
        notifyListeners(currentStrategy, mode)
    }
    
    /**
     * Reset to automatic strategy determination
     */
    fun resetToAutomatic() {
        determineOptimalStrategy()
    }
    
    private fun determineOptimalStrategy() {
        scope.launch {
            currentStrategy = RenderingStrategy.DETERMINING
            
            // Build fallback chain based on device capabilities
            fallbackChain = buildFallbackChain()
            
            // Test each mode in order of preference
            var workingMode: EmojiRenderingMode? = null
            var strategy = RenderingStrategy.LEGACY
            
            for (mode in fallbackChain) {
                if (testRenderingMode(mode)) {
                    workingMode = mode
                    strategy = if (mode == fallbackChain.first()) {
                        RenderingStrategy.OPTIMAL
                    } else {
                        RenderingStrategy.FALLBACK
                    }
                    break
                }
            }
            
            currentStrategy = strategy
            workingMode?.let { mode ->
                notifyListeners(strategy, mode)
            }
            
            Timber.i("Emoji rendering strategy determined: $strategy with mode: $workingMode")
        }
    }
    
    private fun buildFallbackChain(): List<EmojiRenderingMode> {
        val chain = mutableListOf<EmojiRenderingMode>()
        
        // Prefer modern approaches first
        when {
            android.os.Build.VERSION.SDK_INT >= 31 -> {
                // Android 12+ has excellent emoji support
                chain.add(EmojiRenderingMode.NATIVE)
                chain.add(EmojiRenderingMode.EMOJI_COMPAT_BUNDLED)
            }
            android.os.Build.VERSION.SDK_INT >= 28 -> {
                // Android 9+ has good EmojiCompat support
                chain.add(EmojiRenderingMode.EMOJI_COMPAT_BUNDLED)
                chain.add(EmojiRenderingMode.NATIVE)
            }
            android.os.Build.VERSION.SDK_INT >= 23 -> {
                // Android 6+ can use EmojiCompat
                chain.add(EmojiRenderingMode.EMOJI_COMPAT_BUNDLED)
            }
        }
        
        // Always include downloadable fonts as an option
        // (will only work with network connectivity)
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            chain.add(EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE)
        }
        
        // Legacy PNG assets as final fallback
        chain.add(EmojiRenderingMode.LEGACY_PNG)
        
        return chain
    }
    
    private fun determineOptimalMode(): EmojiRenderingMode {
        return fallbackChain.firstOrNull() ?: EmojiRenderingMode.LEGACY_PNG
    }
    
    private fun getFallbackMode(): EmojiRenderingMode {
        return fallbackChain.getOrNull(1) ?: EmojiRenderingMode.LEGACY_PNG
    }
    
    private fun notifyListeners(strategy: RenderingStrategy, mode: EmojiRenderingMode) {
        listeners.forEach { listener ->
            try {
                listener.onStrategyChanged(strategy, mode)
            } catch (e: Exception) {
                Timber.w(e, "Error notifying strategy change listener")
            }
        }
    }
    
    /**
     * Get performance metrics for the current strategy
     */
    fun getPerformanceMetrics(): PerformanceMetrics {
        return PerformanceMetrics(
            strategy = currentStrategy,
            mode = getCurrentRenderingMode(),
            estimatedMemoryUsage = estimateMemoryUsage(),
            estimatedRenderTime = estimateRenderTime()
        )
    }
    
    private fun estimateMemoryUsage(): Long {
        return when (getCurrentRenderingMode()) {
            EmojiRenderingMode.NATIVE -> 1024L // Very low - just text
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED -> 1024L * 100L // Font data
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> 1024L * 150L // Downloaded fonts
            EmojiRenderingMode.LEGACY_PNG -> 1024L * 1024L * 20L // ~20MB for all PNG assets
        }
    }
    
    private fun estimateRenderTime(): Long {
        return when (getCurrentRenderingMode()) {
            EmojiRenderingMode.NATIVE -> 1L // Fastest - native text rendering
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED -> 5L // Fast - processed text
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> 10L // Slightly slower
            EmojiRenderingMode.LEGACY_PNG -> 50L // Slowest - image loading
        }
    }
    
    data class PerformanceMetrics(
        val strategy: RenderingStrategy,
        val mode: EmojiRenderingMode,
        val estimatedMemoryUsage: Long, // in bytes
        val estimatedRenderTime: Long   // in milliseconds
    )
}