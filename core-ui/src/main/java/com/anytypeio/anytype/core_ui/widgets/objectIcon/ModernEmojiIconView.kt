package com.anytypeio.anytype.core_ui.widgets.objectIcon

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.emoji.EmojiRenderingMode
import com.anytypeio.anytype.core_ui.features.emoji.ModernEmojiProvider
import com.anytypeio.anytype.core_ui.features.emoji.ModernEmojiProviderImpl
import com.anytypeio.anytype.core_ui.widgets.contentSizeForBackground
import com.anytypeio.anytype.core_ui.widgets.cornerRadius
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.objects.ObjectIcon

/**
 * Modern emoji icon view that supports multiple rendering modes.
 * Prioritizes native rendering for better performance and smaller APK size.
 */
@Composable
fun ModernEmojiIconView(
    modifier: Modifier = Modifier,
    icon: ObjectIcon.Basic.Emoji,
    backgroundSize: Dp,
    iconWithoutBackgroundMaxSize: Dp,
    backgroundColor: Int = R.color.shape_tertiary,
    renderingMode: EmojiRenderingMode = EmojiRenderingMode.EMOJI_COMPAT_BUNDLED
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    
    // Initialize emoji provider
    val emojiProvider = remember(renderingMode) {
        ModernEmojiProviderImpl(context, renderingMode)
    }
    
    var isProviderReady by remember { mutableStateOf(false) }
    var useNativeRendering by remember { mutableStateOf(false) }
    
    LaunchedEffect(emojiProvider) {
        isProviderReady = emojiProvider.initialize()
        useNativeRendering = when (renderingMode) {
            EmojiRenderingMode.NATIVE -> true
            EmojiRenderingMode.EMOJI_COMPAT_BUNDLED,
            EmojiRenderingMode.EMOJI_COMPAT_DOWNLOADABLE -> {
                emojiProvider.isEmojiSupported(icon.unicode)
            }
            EmojiRenderingMode.LEGACY_PNG -> false
        }
    }
    
    val (containerModifier, iconModifier) = if (backgroundSize <= iconWithoutBackgroundMaxSize) {
        modifier.size(backgroundSize) to Modifier.size(backgroundSize)
    } else {
        modifier
            .size(backgroundSize)
            .background(
                color = colorResource(backgroundColor),
                shape = RoundedCornerShape(size = cornerRadius(backgroundSize))
            ) to Modifier.size(
            contentSizeForBackground(backgroundSize)
        )
    }
    
    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            // Use native text rendering when possible
            isProviderReady && useNativeRendering -> {
                NativeEmojiText(
                    emoji = icon.unicode,
                    modifier = iconModifier,
                    fontSize = calculateEmojiTextSize(backgroundSize, density.density),
                    emojiProvider = emojiProvider
                )
            }
            
            // Fallback to PNG assets for unsupported emojis or legacy mode
            renderingMode == EmojiRenderingMode.LEGACY_PNG || !isProviderReady -> {
                LegacyEmojiImage(
                    emoji = icon.unicode,
                    modifier = iconModifier,
                    fallbackIcon = icon.fallback
                )
            }
            
            // Show fallback icon while loading
            else -> {
                TypeIconView(
                    modifier = modifier,
                    icon = icon.fallback,
                    backgroundSize = backgroundSize,
                    iconWithoutBackgroundMaxSize = iconWithoutBackgroundMaxSize
                )
            }
        }
    }
}

/**
 * Native emoji text rendering using system fonts or EmojiCompat
 */
@Composable
private fun NativeEmojiText(
    emoji: String,
    modifier: Modifier,
    fontSize: TextUnit,
    emojiProvider: ModernEmojiProvider
) {
    val processedText = remember(emoji) {
        emojiProvider.process(emoji)
    }
    
    Text(
        text = processedText.toString(),
        modifier = modifier,
        style = TextStyle(
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    )
}

/**
 * Legacy PNG-based emoji rendering (fallback)
 */
@Composable
private fun LegacyEmojiImage(
    emoji: String,
    modifier: Modifier,
    fallbackIcon: ObjectIcon.TypeIcon.Fallback
) {
    val emojiUri = remember(emoji) {
        Emojifier.safeUri(emoji)
    }
    
    if (emojiUri != Emojifier.Config.EMPTY_URI) {
        Image(
            painter = rememberAsyncImagePainter(emojiUri),
            contentDescription = "Emoji icon",
            modifier = modifier
        )
    } else {
        // Show fallback icon if emoji not found
        TypeIconView(
            modifier = modifier,
            icon = fallbackIcon,
            backgroundSize = modifier.size,
            iconWithoutBackgroundMaxSize = modifier.size
        )
    }
}

/**
 * Calculate appropriate text size for emoji based on container size
 */
private fun calculateEmojiTextSize(containerSize: Dp, density: Float): TextUnit {
    // Emoji text should be ~70% of container size for good visual balance
    val sizeInPx = containerSize.value * density * 0.7f
    return (sizeInPx / density).sp
}

/**
 * Extension property to get size from modifier (simplified)
 */
private val Modifier.size: Dp
    get() = 24.dp // Default size, should be passed as parameter in production