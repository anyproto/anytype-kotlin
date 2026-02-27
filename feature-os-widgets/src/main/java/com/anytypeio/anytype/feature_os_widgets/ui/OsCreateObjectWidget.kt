package com.anytypeio.anytype.feature_os_widgets.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.anytypeio.anytype.feature_os_widgets.R
import com.anytypeio.anytype.feature_os_widgets.deeplink.OsWidgetDeepLinks
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber

private const val TAG = "OsCreateObjectWidget"

/**
 * Glance App Widget that provides a quick-create button for a specific object type.
 * Tapping the widget creates a new object and opens it in the app.
 */
class OsCreateObjectWidget : GlanceAppWidget() {

    companion object {
        // Retry configuration for handling race condition with config activity.
        // Give user up to 30 seconds to configure the widget.
        private const val MAX_RETRIES = 60
        private const val RETRY_DELAY_MS = 500L
        private val SMALL_SIZE = DpSize(57.dp, 57.dp)
        private val MEDIUM_SIZE = DpSize(110.dp, 110.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        try {
            val appContext = context.applicationContext
            val dataStore = OsWidgetsDataStore(appContext)
            val appWidgetId = GlanceAppWidgetManager(appContext).getAppWidgetId(id)

            // Retry logic to handle race condition where config might not be persisted yet
            val config = getConfigWithRetry(dataStore, appWidgetId)

            provideContent {
                GlanceTheme {
                    WidgetContent(config = config, size = LocalSize.current)
                }
            }
        } catch (e: CancellationException) {
            // Rethrow cancellation exceptions to allow proper coroutine cancellation
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "provideGlance failed")
        }
    }

    /**
     * Attempts to get the widget configuration with retries.
     * 
     * This handles the race condition where provideGlance might be called
     * before the config activity has finished persisting the configuration.
     */
    private suspend fun getConfigWithRetry(
        dataStore: OsWidgetsDataStore,
        appWidgetId: Int
    ): OsWidgetCreateObjectEntity? {
        repeat(MAX_RETRIES) { attempt ->
            dataStore.getCreateObjectConfig(appWidgetId)?.let { return it }
            if (attempt < MAX_RETRIES - 1) {
                delay(RETRY_DELAY_MS)
            }
        }
        Timber.tag(TAG).d("Config not found for widget $appWidgetId after $MAX_RETRIES attempts")
        return null
    }
}

@Composable
private fun WidgetContent(config: OsWidgetCreateObjectEntity?, size: DpSize) {
    val isSmall = size.width < 100.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetBackgroundColor)
            .padding(if (isSmall) 4.dp else 8.dp)
    ) {
        if (config == null) {
            NotConfiguredState(isSmall = isSmall)
        } else {
            CreateObjectCard(config = config, size = size)
        }
    }
}

@Composable
private fun NotConfiguredState(isSmall: Boolean) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isSmall) {
            Image(
                provider = ImageProvider(R.drawable.ic_anytype_logo_widget),
                contentDescription = null,
                modifier = GlanceModifier.size(32.dp)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_anytype_logo_widget),
                    contentDescription = null,
                    modifier = GlanceModifier.size(48.dp)
                )
                Spacer(modifier = GlanceModifier.height(12.dp))
                Text(
                    text = "Widget not configured",
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextSecondary),
                        fontSize = 14.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun CreateObjectCard(config: OsWidgetCreateObjectEntity, size: DpSize) {
    val deepLink = OsWidgetDeepLinks.buildCreateObjectDeepLink(config.appWidgetId)
    val intent = OsWidgetDeepLinks.buildCreateObjectIntent(config.appWidgetId)
    Timber.tag(TAG).d("CreateObjectCard: appWidgetId=${config.appWidgetId}, spaceId=${config.spaceId}, typeKey=${config.typeKey}, deepLink=$deepLink")

    val isSmall = size.width < 100.dp
    // Adaptive sizes based on widget dimensions
    val iconSize = (size.width.value * 0.35f).coerceIn(24f, 48f).dp
    val emojiFontSize = (iconSize.value * 0.65f).coerceIn(16f, 32f).sp
    val initialFontSize = (iconSize.value * 0.5f).coerceIn(12f, 24f).sp
    val nameFontSize = (size.width.value * 0.12f).coerceIn(11f, 14f).sp
    val padding = (size.width.value * 0.08f).coerceIn(4f, 12f).dp
    val cornerRadius = if (isSmall) 8.dp else 12.dp

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(OsWidgetSurfaceColor)
            .cornerRadius(cornerRadius)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center
    ) {
        if (isSmall) {
            // Compact layout - icon + type name
            Column(
                modifier = GlanceModifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypeIcon(
                    config = config,
                    iconSize = iconSize,
                    emojiFontSize = emojiFontSize,
                    initialFontSize = initialFontSize
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = config.typeName.ifEmpty { "Object" },
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextPrimary),
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        } else {
            // Full layout
            Column(
                modifier = GlanceModifier.padding(padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TypeIcon(
                    config = config,
                    iconSize = iconSize,
                    emojiFontSize = emojiFontSize,
                    initialFontSize = initialFontSize
                )

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Type name
                Text(
                    text = config.typeName.ifEmpty { "Object" },
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextPrimary),
                        fontSize = nameFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )

                // Space name (secondary)
                if (config.spaceName.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = config.spaceName,
                        style = TextStyle(
                            color = ColorProvider(OsWidgetTextSecondary),
                            fontSize = 12.sp
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = GlanceModifier.height(6.dp))

                // Create action hint
                Text(
                    text = "Tap to create",
                    style = TextStyle(
                        color = ColorProvider(OsWidgetTextTertiary),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun TypeIcon(
    config: OsWidgetCreateObjectEntity,
    iconSize: androidx.compose.ui.unit.Dp,
    emojiFontSize: androidx.compose.ui.unit.TextUnit,
    initialFontSize: androidx.compose.ui.unit.TextUnit
) {
    val emoji = config.typeIconEmoji
    val hasCustomIcon = !config.typeIconName.isNullOrEmpty()
    val iconCornerRadius = (iconSize.value * 0.16f).coerceIn(4f, 8f).dp

    if (!emoji.isNullOrEmpty()) {
        // Show emoji
        Text(
            text = emoji,
            style = TextStyle(
                fontSize = emojiFontSize
            )
        )
    } else if (hasCustomIcon) {
        // Custom icon type - show colored placeholder
        val iconColor = getIconColor(config.typeIconOption)
        val initial = config.typeName.firstOrNull()?.uppercaseChar()?.toString() ?: "+"
        Box(
            modifier = GlanceModifier
                .size(iconSize)
                .cornerRadius(iconCornerRadius)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(iconColor),
                    fontSize = initialFontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    } else {
        // Fallback placeholder
        val initial = config.typeName.firstOrNull()?.uppercaseChar()?.toString() ?: "+"
        Box(
            modifier = GlanceModifier
                .size(iconSize)
                .cornerRadius(iconCornerRadius)
                .background(OsWidgetIconPlaceholderColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                style = TextStyle(
                    color = ColorProvider(OsWidgetTextPrimary),
                    fontSize = initialFontSize,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Maps icon option to a color for the widget.
 * Icon option values: 1=Gray, 2=Yellow, 3=Amber, 4=Red, 5=Pink, 6=Purple, 7=Blue, 8=Sky, 9=Teal, 10=Green
 */
private fun getIconColor(iconOption: Int?): androidx.compose.ui.graphics.Color {
    return when (iconOption) {
        1 -> OsWidgetIconGray
        2 -> OsWidgetIconYellow
        3 -> OsWidgetIconAmber
        4 -> OsWidgetIconRed
        5 -> OsWidgetIconPink
        6 -> OsWidgetIconPurple
        7 -> OsWidgetIconBlue
        8 -> OsWidgetIconSky
        9 -> OsWidgetIconTeal
        10 -> OsWidgetIconGreen
        else -> OsWidgetIconGray // Default
    }
}
