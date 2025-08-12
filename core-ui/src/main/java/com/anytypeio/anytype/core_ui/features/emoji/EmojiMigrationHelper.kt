package com.anytypeio.anytype.core_ui.features.emoji

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.objects.ObjectIcon

/**
 * Migration helper for gradually introducing modern emoji rendering.
 * This component can be used as a drop-in replacement for existing EmojiIconView
 * while providing the ability to enable modern rendering via configuration.
 */

/**
 * Unified emoji icon component that switches between old and new implementations
 * based on configuration. This allows for A/B testing and gradual rollout.
 */
@Composable
fun UnifiedEmojiIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Emoji,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp,
    backgroundColor: Int = R.color.shape_tertiary,
    forceModern: Boolean? = null // Override for testing
) {
    val context = LocalContext.current
    val emojiConfig = remember { EmojiConfiguration(context) }
    val configuration by emojiConfig.configurationFlow.collectAsState(
        initial = EmojiConfiguration.Configuration(
            modernEmojiEnabled = false,
            renderingMode = EmojiRenderingMode.LEGACY_PNG,
            autoFallbackEnabled = true,
            performanceMode = false,
            accessibilityMode = false
        )
    )
    
    // Determine which renderer to use
    val useModern = forceModern ?: (configuration.modernEmojiEnabled && 
                                   EmojiFeatureConfig.isModernEmojiAvailable())
    
    EmojiRenderer.EmojiIcon(
        modifier = modifier,
        icon = icon,
        backgroundSize = backgroundSize,
        iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize,
        backgroundColor = backgroundColor,
        config = EmojiRenderer.Config(
            useModernRenderer = useModern,
            renderingMode = configuration.getEffectiveRenderingMode(),
            enableFallback = configuration.autoFallbackEnabled
        )
    )
}

/**
 * Demo component to showcase different emoji rendering modes
 * This can be used in development screens to test emoji rendering
 */
@Composable
fun EmojiRenderingDemo(
    modifier: Modifier = Modifier,
    emoji: String = "😀",
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp
) {
    val testIcon = ObjectIcon.Basic.Emoji(
        unicode = emoji,
        fallback = ObjectIcon.TypeIcon.Fallback.DEFAULT
    )
    
    // Show all rendering modes for comparison
    EmojiRenderingMode.values().forEach { mode ->
        UnifiedEmojiIconView(
            modifier = modifier,
            icon = testIcon,
            backgroundSize = backgroundSize,
            iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize,
            forceModern = mode != EmojiRenderingMode.LEGACY_PNG
        )
    }
}

/**
 * Performance testing helper
 */
object EmojiPerformanceTester {
    
    data class TestResult(
        val mode: EmojiRenderingMode,
        val initTime: Long,
        val renderTime: Long,
        val memoryUsage: Long,
        val success: Boolean,
        val error: String? = null
    )
    
    /**
     * Test performance of different emoji rendering modes
     */
    suspend fun runPerformanceTest(
        context: android.content.Context,
        testEmojis: List<String> = listOf("😀", "👍", "🎉", "❤️", "🔥")
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()
        
        for (mode in EmojiRenderingMode.values()) {
            try {
                val startTime = System.currentTimeMillis()
                val provider = ModernEmojiProviderImpl(context, mode)
                val initSuccess = provider.initialize()
                val initTime = System.currentTimeMillis() - startTime
                
                if (initSuccess) {
                    val renderStartTime = System.currentTimeMillis()
                    var renderSuccess = true
                    
                    // Test rendering each emoji
                    for (emoji in testEmojis) {
                        try {
                            provider.process(emoji)
                        } catch (e: Exception) {
                            renderSuccess = false
                            break
                        }
                    }
                    
                    val renderTime = System.currentTimeMillis() - renderStartTime
                    
                    results.add(TestResult(
                        mode = mode,
                        initTime = initTime,
                        renderTime = renderTime,
                        memoryUsage = estimateMemoryUsage(mode),
                        success = renderSuccess
                    ))
                } else {
                    results.add(TestResult(
                        mode = mode,
                        initTime = initTime,
                        renderTime = -1,
                        memoryUsage = -1,
                        success = false,
                        error = "Initialization failed"
                    ))
                }
            } catch (e: Exception) {
                results.add(TestResult(
                    mode = mode,
                    initTime = -1,
                    renderTime = -1,
                    memoryUsage = -1,
                    success = false,
                    error = e.message
                ))
            }
        }
        
        return results
    }
    
    private fun estimateMemoryUsage(mode: EmojiRenderingMode): Long {
        return when (mode) {
            EmojiRenderingMode.NATIVE -> 1024L
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED -> 1024L * 100L
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> 1024L * 150L
            EmojiRenderingMode.LEGACY_PNG -> 1024L * 1024L * 20L
        }
    }
}